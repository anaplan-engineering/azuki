package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbPurse_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbWorld_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.property
import com.anaplan.engineering.kazuki.core.*

@Module
interface AbWorld {

    val abAuthPurse: Mapping<Name, AbPurse>

    @FunctionProvider(AbWorldFunctions::class)
    val functions: AbWorldFunctions
}

// * Z pres are implicit. Get them from ZEVES-PRG126 Table 8.1 p.86
// * Provider contains before state; expects opertions to return after state and AOut
// * Keep pres/post explicit for matching with the Z
class AbWorldFunctions(abWorld: AbWorld) {

    // Signature for all operations as: AbWorld.() -> VFunction1<AIn, Tuple2<AbWorld, aNullOut>>
    val abOp = function (
        command = { _: AIn ->
            //LF @EK this could be "any world" not just keeping it the same
            mk_(abWorld, aNullOut) },
        // Keep explicit here as in ZEVES-PRG126 Table 8.1 p.86
        pre = { _ -> true },
        post = { _, result ->
            val (_, abang) = result
            abang == aNullOut
        }
    )

    val abIgnore = function (
        command = { a: AIn ->
            abOp(a)
        },
        // Explicitly call all related pres, given Z's implicit pres
        // This is important to ensure the function captures the intended Z
        pre = { a -> abOp.pre(a) },
        post = { a, result ->
            val (dash, abang) = result
            abOp.post(a, result) &&
            dash.abAuthPurse == abWorld.abAuthPurse
        }
    )

    //LF @QST Example of degenarate hiding (hides everything), the xiAbPurseTransfer is innocuous; ignoring it
    //val abPurseTransfer = function ()

    val abWorldSecureOp = function (
        command = { a: AIn, td: TransferDetails ->
            abOp(a)
        },
        pre = { a, td ->
            abOp.pre(a)
                //LF @QST Not sure this is right; discuss with AP; we want the encoding of the inverse of transfer
                && a is transfer
                && a.transferDetails == td
        },
        post = { a, td, result ->
            val (dash, _) = result
            abOp.post(a, result) &&
            dash.abAuthPurse.domSubtract(mk_Set(td.from, td.to)) ==
                abWorld.abAuthPurse.domSubtract(mk_Set(td.from, td.to))
        }
    )

    val abTransferOkayTD = function (
        command = { a: AIn, td: TransferDetails ->
            val (dash, abang) = abWorldSecureOp(a, td)
            mk_(dash.transform(
                // Corresponds to the Z \mu \Delta AbPurse operation
                abAuthPurse = abWorld.abAuthPurse * mk_Mapping(
                    mk_(td.from, abWorld.abAuthPurse[td.from].transform(balance = abWorld.abAuthPurse[td.from].balance - td.value)),
                    mk_(td.to, abWorld.abAuthPurse[td.to].transform(balance = abWorld.abAuthPurse[td.to].balance + td.value))
                )), abang)
        },
        pre = { a, td ->
            abWorldSecureOp.pre(a, td)
                && authentic(td.from)
                && authentic(td.to)
                && sourceHasSufficientFunds(td)
                && td.from != td.to
        },
        post = { a, td, result ->
            val (dash, _) = result
            abWorldSecureOp.post(a, td, result)
                && dash.abAuthPurse[td.from].balance == abWorld.abAuthPurse[td.from].balance - td.value
                && dash.abAuthPurse[td.from].lost == abWorld.abAuthPurse[td.from].lost
                && dash.abAuthPurse[td.to].balance == abWorld.abAuthPurse[td.to].balance + td.value
                && dash.abAuthPurse[td.to].lost == abWorld.abAuthPurse[td.to].lost
        }
    )

    val abTransferLostTD = function (
        command = { a: AIn, td: TransferDetails ->
            val (dash, abang) = abWorldSecureOp(a, td)
            mk_(abWorld.transform(
                abAuthPurse = abWorld.abAuthPurse * mk_(
                    td.from, abWorld.abAuthPurse[td.from].transform(
                        balance = abWorld.abAuthPurse[td.from].balance - td.value,
                        lost = abWorld.abAuthPurse[td.from].lost + td.value))),
                    abang)
        },
        pre = { a, td ->
            abWorldSecureOp.pre(a, td)
                && authentic(td.from)
                && authentic(td.to)
                && sourceHasSufficientFunds(td)
                && td.from != td.to
        },
        post = { a, td, result ->
            val (dash, _) = result
            abWorldSecureOp.post(a, td, result)
                && dash.abAuthPurse[td.from].balance == abWorld.abAuthPurse[td.from].balance - td.value
                && dash.abAuthPurse[td.from].lost == abWorld.abAuthPurse[td.from].lost + td.value
                && dash.abAuthPurse[td.to] == abWorld.abAuthPurse[td.to]
        }
    )

    private val authentic = function (
        command = { name: Name ->
            name in abWorld.abAuthPurse.dom
        }
    )

    private val sourceHasSufficientFunds = function (
        command = { td: TransferDetails ->
            td.value <= abWorld.abAuthPurse[td.from].balance
        }
    )

    private val totalBalance = function (
        command = { authPurse: Mapping<Name, AbPurse> ->
            authPurse.rng.fold(0uL) { acc, purse -> acc + purse.balance}
        }
    )

    private val totalLost = function (
        command = { authPurse: Mapping<Name, AbPurse> ->
            authPurse.rng.fold(0uL) { acc, purse -> acc + purse.lost}
        }
    )

    val noValueCreation = function (
        command = { after: AbWorld ->
            //LF @QST something has to happen to the AbWorld, but shouldnt' a boolean property check, that's the post!
            //        namely, in previous commands you "choose" an implementation, here you can't "test" for a given one?
            after
        },
        post = { _, result: AbWorld -> totalBalance(abWorld.abAuthPurse) <= totalBalance(result.abAuthPurse)
        }
    )

    val allValueAccounted = function (
        command = { after: AbWorld ->
            after
        },
        post = { _, result ->
            totalBalance(result.abAuthPurse) + totalLost(result.abAuthPurse) ==
                totalBalance(abWorld.abAuthPurse) + totalLost(abWorld.abAuthPurse)
        }
    )
}
