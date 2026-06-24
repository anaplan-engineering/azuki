package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.World_Module.transform
import com.anaplan.engineering.kazuki.core.*

typealias Name = String

sealed interface AOut
object aNullOut: AOut

@Module
interface World {
    val authPurses: Mapping<Name, Purse>

    @FunctionProvider(WorldFunctions::class)
    val functions: WorldFunctions
}

class WorldFunctions(world: World) {
    val abstractOperation = function (
        command = { a: AbstractInput -> world }
    )

    val abstractIgnore = function (
        command = { a: AbstractInput ->
            abstractOperation(a)
        },
        pre = { a ->
            abstractOperation.pre(a)
        },
        post = { _, result: World ->
            result.authPurses == world.authPurses
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
                world.authPurses.domSubtract(mk_Set(transferDetails.from, transferDetails.to))
        }
    )

    val abstractTransferOkayTD = function (
        command = { a: AbstractInput, transferDetails: TransferDetails ->
            world.transform(
                authPurses = world.authPurses * mk_Mapping(
                    mk_(transferDetails.from, world.authPurses[transferDetails.from].transform(balance = world.authPurses[transferDetails.from].balance - transferDetails.value)),
                    mk_(transferDetails.to, world.authPurses[transferDetails.to].transform(balance = world.authPurses[transferDetails.to].balance + transferDetails.value))
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
        post = { a, transferDetails, result: World ->
            abstractWorldSecureOperation.post(a, transferDetails, result)
                && result.authPurses[transferDetails.from].balance == world.authPurses[transferDetails.from].balance - transferDetails.value
                && result.authPurses[transferDetails.from].lost == world.authPurses[transferDetails.from].lost
                && result.authPurses[transferDetails.to].balance == world.authPurses[transferDetails.to].balance + transferDetails.value
                && result.authPurses[transferDetails.to].lost == world.authPurses[transferDetails.to].lost
        }
    )

    val abstractTransferLostTD = function (
        command = { a: AbstractInput, transferDetails: TransferDetails ->
            world.transform(
                authPurses = world.authPurses * mk_(
                    transferDetails.from, world.authPurses[transferDetails.from].transform(balance = world.authPurses[transferDetails.from].balance - transferDetails.value, lost = world.authPurses[transferDetails.from].lost + transferDetails.value)
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
        post = { a, transferDetails, result: World ->
            abstractWorldSecureOperation.post(a, transferDetails, result)
                && result.authPurses[transferDetails.from].balance == world.authPurses[transferDetails.from].balance - transferDetails.value
                && result.authPurses[transferDetails.from].lost == world.authPurses[transferDetails.from].lost + transferDetails.value
                && result.authPurses[transferDetails.to] == world.authPurses[transferDetails.to]
        }
    )

    private val purseIsAuthentic = function (
        command = { name: Name ->
            name in world.authPurses.dom
        }
    )

    private val sourceHasSufficientFunds = function (
        command = { transferDetails: TransferDetails ->
            transferDetails.value <= world.authPurses[transferDetails.from].balance
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
            totalBalance(beforeWorldAuthPurses) >= totalBalance(world.authPurses)
        }
    )

    val allValueAccounted = function (
        command = { beforeWorldAuthPurses: Mapping<Name, Purse> ->
            totalBalance(beforeWorldAuthPurses) + totalLost(beforeWorldAuthPurses) ==
                totalBalance(world.authPurses) + totalLost(world.authPurses)
        }
    )

}
