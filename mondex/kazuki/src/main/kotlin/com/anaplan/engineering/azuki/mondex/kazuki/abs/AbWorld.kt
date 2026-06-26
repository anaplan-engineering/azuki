package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.Purse
import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails
import com.anaplan.engineering.azuki.mondex.kazuki.World
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbPurse_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbWorld_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.property
import com.anaplan.engineering.kazuki.core.*

@Module
interface AbWorld : World {

    @FunctionProvider(AbWorldFunctions::class)
    val functions: AbWorldFunctions

    @FunctionProvider(AbWorldProperties::class)
    val properties: AbWorldProperties
}

// Z (inferred) properties of the schemas
class AbWorldProperties(abWorld: AbWorld) {

    //LF @QST for refinement, maybe allow a map here with both purses within and just filter?
    @Suppress("UNCHECKED_CAST")
    val abAuthPurse by property(pre = { -> forall(abWorld.purses.rng) { it -> it is AbPurse } }) { abWorld.purses as Mapping<Name, AbPurse> }
}

// * Z pres are implicit. Get them from ZEVES-PRG126 Table 8.1 p.86
// * Provider contains before state; expects opertions to return after state and AOut
// * Keep pres/post explicit for matching with the Z
class AbWorldFunctions(abWorld: AbWorld) {

    // Signature for all operations as: AbWorld.() -> VFunction1<AIn, Tuple2<AbWorld, aNullOut>>
    val abOp = function (
        command = { _: AIn ->
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
            dash.properties.abAuthPurse == abWorld.properties.abAuthPurse
        }
    )

    val abWorldSecureOp = function (
        command = { a: AIn, td: TransferDetails ->
            abOp(a)
        },
        pre = { a, td ->
            abOp.pre(a)
                //LF @QST Not sure this is right; discuss with AP; we want the encoding of the inverse of transfer
                //&&a is Transfer
                //&& a.td == td
                && a is transfer
                && transfer(td) == a
        },
        post = { a, td, result ->
            val (dash, abang) = result
            abOp.post(a, result) &&
            dash.properties.abAuthPurse.domSubtract(mk_Set(td.from, td.to)) ==
                abWorld.properties.abAuthPurse.domSubtract(mk_Set(td.from, td.to))
        }
    )

    val abTransferOkayTD = function (
        command = { a: AIn, td: TransferDetails ->
            val (dash, abang) = abWorldSecureOp(a, td)
            mk_(dash.transform(
                purses = abWorld.properties.abAuthPurse * mk_Mapping(
                    mk_(td.from, abWorld.properties.abAuthPurse[td.from].transform(balance = abWorld.properties.abAuthPurse[td.from].balance - td.value)),
                    mk_(td.to, abWorld.properties.abAuthPurse[td.to].transform(balance = abWorld.properties.abAuthPurse[td.to].balance + td.value))
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
            val (dash, abang) = result
            abWorldSecureOp.post(a, td, result)
                && dash.properties.abAuthPurse[td.from].balance == abWorld.properties.abAuthPurse[td.from].balance - td.value
                && dash.properties.abAuthPurse[td.from].lost == abWorld.properties.abAuthPurse[td.from].lost
                && dash.properties.abAuthPurse[td.to].balance == abWorld.properties.abAuthPurse[td.to].balance + td.value
                && dash.properties.abAuthPurse[td.to].lost == abWorld.properties.abAuthPurse[td.to].lost
        }
    )

    val abTransferLostTD = function (
        command = { a: AIn, td: TransferDetails ->
            val (dash, abang) = abWorldSecureOp(a, td)
            mk_(abWorld.transform(
                purses = abWorld.properties.abAuthPurse * mk_(
                    td.from, abWorld.properties.abAuthPurse[td.from].transform(
                        balance = abWorld.properties.abAuthPurse[td.from].balance - td.value,
                        lost = abWorld.properties.abAuthPurse[td.from].lost + td.value))),
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
            val (dash, abang) = result
            abWorldSecureOp.post(a, td, result)
                && dash.properties.abAuthPurse[td.from].balance == abWorld.properties.abAuthPurse[td.from].balance - td.value
                && dash.properties.abAuthPurse[td.from].lost == abWorld.properties.abAuthPurse[td.from].lost + td.value
                && dash.properties.abAuthPurse[td.to] == abWorld.properties.abAuthPurse[td.to]
        }
    )

    private val authentic = function (
        command = { name: Name ->
            name in abWorld.properties.abAuthPurse.dom
        }
    )

    private val sourceHasSufficientFunds = function (
        command = { td: TransferDetails ->
            td.value <= abWorld.properties.abAuthPurse[td.from].balance
        }
    )

    // This will work on both AbPurse and ConPurse, given they share balance field
    private val totalBalance = function (
        command = { authPurses: Mapping<Name, Purse> ->
            authPurses.rng.fold(0uL) { acc, purse -> acc + purse.balance}
        }
    )

    private val totalLost = function (
        command = { authPurses: Mapping<Name, AbPurse> ->
            authPurses.rng.fold(0uL) { acc, purse -> acc + purse.lost}
        }
    )

    val noValueCreation = function (
        command = { ->
            //LF @QST something has to happen to the AbWorld, but shouldnt' a boolean property check, that's the post!
            //        namely, in previous commands you "choose" an implementation, here you can't "test" for a given one?
            abWorld
        },
        pre = { -> true },
        post = { dash -> totalBalance(abWorld.properties.abAuthPurse) <= totalBalance(dash.properties.abAuthPurse)
        }
    )

    val allValueAccounted = function (
        command = { ->
            abWorld
        },
        post = { dash ->
            totalBalance(dash.properties.abAuthPurse) + totalLost(dash.properties.abAuthPurse) ==
                totalBalance(abWorld.properties.abAuthPurse) + totalLost(abWorld.properties.abAuthPurse)
        }
    )
}
