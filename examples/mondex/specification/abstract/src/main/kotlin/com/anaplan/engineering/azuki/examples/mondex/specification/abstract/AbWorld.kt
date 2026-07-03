package com.anaplan.engineering.azuki.examples.mondex.specification.abstract

import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbPurse_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbWorld_Module.transform
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.SecurityProperties.authentic
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.SecurityProperties.sufficientFunds
import com.anaplan.engineering.kazuki.core.*

@Module
interface AbWorld {

    val abAuthPurse: Mapping<Name, AbPurse>

    @FunctionProvider(AbWorldProperties::class)
    val properties: AbWorldProperties

    @FunctionProvider(AbWorldFunctions::class)
    val functions: AbWorldFunctions
}

class AbWorldProperties(abWorld: AbWorld) {

    val totalBalance by property { abWorld.abAuthPurse.rng.sumOf { it.balance } }

    val totalLost by property { abWorld.abAuthPurse.rng.sumOf { it.lost } }

    val totalValue by property { totalBalance + totalLost }

}

// * Z pres are implicit. Get them from ZEVES-PRG126 Table 8.1 p.86
// * Provider contains before state; expects opertions to return after state and AOut
// * Keep pres/post explicit for matching with the Z
class AbWorldFunctions(abWorld: AbWorld) {

    // Signature for all operations as: AbWorld.() -> VFunction1<AIn, Tuple2<AbWorld, aNullOut>>
    val abOp = function(
        command = { _: AIn ->
            //LF @EK this could be "any world" not just keeping it the same
            mk_(abWorld, aNullOut)
        },
        // Keep explicit here as in ZEVES-PRG126 Table 8.1 p.86
        pre = { _ -> true },
        post = { _, result ->
            val (_, abang) = result
            abang == aNullOut
        }
    )

    val abIgnore = function(
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

    val abWorldSecureOp = function(
        command = { a: AIn, td: TransferDetails ->
            abOp(a)
        },
        pre = { a, td ->
            abOp.pre(a)
                //LF @QST Not sure this is right; discuss with AP; we want the encoding of the inverse of transfer
                && a is Transfer
                && a.transferDetails == td
        },
        post = { a, td, result ->
            val (dash, _) = result
            abOp.post(a, result) &&
                dash.abAuthPurse.domSubtract(mk_Set(td.from, td.to)) ==
                abWorld.abAuthPurse.domSubtract(mk_Set(td.from, td.to))
        }
    )

    val abTransferOkayTD = function(
        command = { a: AIn, td: TransferDetails ->
            val (dash, abang) = abWorldSecureOp(a, td)
            mk_(dash.transform(
                // Corresponds to the Z \mu \Delta AbPurse operation
                abAuthPurse = abWorld.abAuthPurse * mk_Mapping(
                    mk_(td.from,
                        abWorld.abAuthPurse[td.from].transform(balance = abWorld.abAuthPurse[td.from].balance - td.value)),
                    mk_(td.to,
                        abWorld.abAuthPurse[td.to].transform(balance = abWorld.abAuthPurse[td.to].balance + td.value))
                )), abang)
        },
        pre = { a, td ->
            abWorldSecureOp.pre(a, td)
                && authentic(abWorld, td.from)
                && authentic(abWorld, td.to)
                && sufficientFunds(abWorld, td)
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

    val abTransferLostTD = function(
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
                && authentic(abWorld, td.from)
                && authentic(abWorld, td.to)
                && sufficientFunds(abWorld, td)
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

}
