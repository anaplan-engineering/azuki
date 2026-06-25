package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.azuki.mondex.kazuki.Purse
import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails
import com.anaplan.engineering.azuki.mondex.kazuki.World_Module.transform
import com.anaplan.engineering.kazuki.core.*

//LF: where did AIn went? you will need it. Keep the Mondex types naming conventions as much as possible for clarity.
sealed interface AIN
object aNullIn: AIN
class transfer(val transferDetails: TransferDetails): AIN

sealed interface AOut
object aNullOut: AOut

@Module
interface AbWorld {
    val authPurses: Mapping<Name, Purse>

    @FunctionProvider(WorldFunctions::class)
    val functions: WorldFunctions
}

class WorldFunctions(abWorld: AbWorld) {
    val abstractOperation = function (
        command = { a: AbstractInput -> abWorld }
    )

    val abstractIgnore = function (
        command = { a: AbstractInput ->
            abstractOperation(a)
        },
        pre = { a ->
            abstractOperation.pre(a)
        },
        post = { _, result: AbWorld ->
            result.authPurses == abWorld.authPurses
        }
    )

    val abstractWorldSecureOperation = function (
        command = { a: AbstractInput, transferDetails: TransferDetails ->
            abstractOperation(a)
        },
        pre = { a, transferDetails ->
            abstractOperation.pre(a)
                && a is Transfer
                && a.transferDetails == transferDetails
        },
        post = { _, transferDetails, result ->
            result.authPurses.domSubtract(mk_Set(transferDetails.from, transferDetails.to)) ==
                abWorld.authPurses.domSubtract(mk_Set(transferDetails.from, transferDetails.to))
        }
    )

    val abstractTransferOkayTD = function (
        command = { a: AbstractInput, transferDetails: TransferDetails ->
            abWorld.transform(
                authPurses = abWorld.authPurses * mk_Mapping(
                    mk_(transferDetails.from, abWorld.authPurses[transferDetails.from].transform(balance = abWorld.authPurses[transferDetails.from].balance - transferDetails.value)),
                    mk_(transferDetails.to, abWorld.authPurses[transferDetails.to].transform(balance = abWorld.authPurses[transferDetails.to].balance + transferDetails.value))
                )
            )
        },
        pre = { a, transferDetails ->
            abstractWorldSecureOperation.pre(a, transferDetails)
                && purseIsAuthentic(transferDetails.from)
                && purseIsAuthentic(transferDetails.to)
                && sourceHasSufficientFunds(transferDetails)
                && transferDetails.from != transferDetails.to
        },
        post = { a, transferDetails, result: AbWorld ->
            abstractWorldSecureOperation.post(a, transferDetails, result)
                && result.authPurses[transferDetails.from].balance == abWorld.authPurses[transferDetails.from].balance - transferDetails.value
                && result.authPurses[transferDetails.from].lost == abWorld.authPurses[transferDetails.from].lost
                && result.authPurses[transferDetails.to].balance == abWorld.authPurses[transferDetails.to].balance + transferDetails.value
                && result.authPurses[transferDetails.to].lost == abWorld.authPurses[transferDetails.to].lost
        }
    )

    val abstractTransferLostTD = function (
        command = { a: AbstractInput, transferDetails: TransferDetails ->
            abWorld.transform(
                authPurses = abWorld.authPurses * mk_(
                    transferDetails.from, abWorld.authPurses[transferDetails.from].transform(balance = abWorld.authPurses[transferDetails.from].balance - transferDetails.value, lost = abWorld.authPurses[transferDetails.from].lost + transferDetails.value)
                )
            )
        },
        pre = { a, transferDetails ->
            abstractWorldSecureOperation.pre(a, transferDetails)
                && purseIsAuthentic(transferDetails.from)
                && purseIsAuthentic(transferDetails.to)
                && sourceHasSufficientFunds(transferDetails)
                && transferDetails.from != transferDetails.to
        },
        post = { a, transferDetails, result: AbWorld ->
            abstractWorldSecureOperation.post(a, transferDetails, result)
                && result.authPurses[transferDetails.from].balance == abWorld.authPurses[transferDetails.from].balance - transferDetails.value
                && result.authPurses[transferDetails.from].lost == abWorld.authPurses[transferDetails.from].lost + transferDetails.value
                && result.authPurses[transferDetails.to] == abWorld.authPurses[transferDetails.to]
        }
    )

    private val purseIsAuthentic = function (
        command = { name: Name ->
            name in abWorld.authPurses.dom
        }
    )

    private val sourceHasSufficientFunds = function (
        command = { transferDetails: TransferDetails ->
            transferDetails.value <= abWorld.authPurses[transferDetails.from].balance
        }
    )

    private val totalBalance = function (
        command = { authPurses: Mapping<Name, Purse> ->
            authPurses.rng.fold(0uL) { acc, purse -> acc + purse.balance}
        }
    )

    private val totalLost = function (
        command = { authPurses: Mapping<Name, Purse> ->
            authPurses.rng.fold(0uL) { acc, purse -> acc + purse.lost}
        }
    )

    val noValueCreation = function (
        command = { beforeWorldAuthPurses: Mapping<Name, Purse> ->
            totalBalance(beforeWorldAuthPurses) >= totalBalance(abWorld.authPurses)
        }
    )

    val allValueAccounted = function (
        command = { beforeWorldAuthPurses: Mapping<Name, Purse> ->
            totalBalance(beforeWorldAuthPurses) + totalLost(beforeWorldAuthPurses) ==
                totalBalance(abWorld.authPurses) + totalLost(abWorld.authPurses)
        }
    )

}
