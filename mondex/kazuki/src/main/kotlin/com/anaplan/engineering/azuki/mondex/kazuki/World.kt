package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.mk_Purse
import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.transform
import com.anaplan.engineering.azuki.mondex.kazuki.World_Module.transform
import com.anaplan.engineering.kazuki.core.*

typealias Name = String

sealed interface AIN
object aNullIn: AIN
data class transfer(val transferDetails: TransferDetails): AIN

sealed interface AOUT
object aNullOut: AOUT

@Module
interface World {
    val authPurses: Mapping<Name, Purse>

    @FunctionProvider(WorldFunctions::class)
    val functions: WorldFunctions
}

class WorldFunctions(world: World) {
    val abstractOperation = function (
        command = { a: AIN -> world }
    )

    val abstractIgnore = function (
        command = { a: AIN ->
            world
        },
        post = { _, result: World ->
            result.authPurses == world.authPurses
        }
    )

    val abstractWorldSecureOperation = function (
        command = { a: AIN, transferDetails: TransferDetails ->
            world
        },
        pre = { a, transferDetails ->
            a is transfer
                && a.transferDetails == transferDetails
        },
        post = { _, transferDetails, result ->
            result.authPurses.domSubtract(mk_Set(transferDetails.from, transferDetails.to)) ==
                world.authPurses.domSubtract(mk_Set(transferDetails.from, transferDetails.to))
        }
    )

    val abstractTransferOkayTD = function (
        command = { a: AIN, transferDetails: TransferDetails ->
            world.transform(
                authPurses = world.authPurses * mk_Mapping(
                    mk_(transferDetails.from, world.authPurses[transferDetails.from].transform(balance = world.authPurses[transferDetails.from].balance - transferDetails.value)),
                    mk_(transferDetails.to, world.authPurses[transferDetails.to].transform(balance = world.authPurses[transferDetails.to].balance + transferDetails.value))
                )
            )
        },
        pre = { a, transferDetails ->
            abstractWorldSecureOperation.pre(a, transferDetails)
                && transferDetails.from in world.authPurses.dom
                && transferDetails.to in world.authPurses.dom
                && world.authPurses[transferDetails.from].balance >= transferDetails.value
        },
        post = { a, transferDetails, result: World ->
            abstractWorldSecureOperation.post(a, transferDetails, result)
                && result.authPurses[transferDetails.from].balance == world.authPurses[transferDetails.from].balance - transferDetails.value + transferDetails.value
                && result.authPurses[transferDetails.from].lost == world.authPurses[transferDetails.from].lost
                && result.authPurses[transferDetails.to].balance == world.authPurses[transferDetails.to].balance + transferDetails.value
                && result.authPurses[transferDetails.to].lost == world.authPurses[transferDetails.to].lost
        }
    )

    val abstractTransferLostTD = function (
        command = { a: AIN, transferDetails: TransferDetails ->
            world.transform(
                authPurses = world.authPurses * mk_(
                    transferDetails.from, world.authPurses[transferDetails.from].transform(lost = world.authPurses[transferDetails.from].lost + transferDetails.value)
                )
            )
        },
        pre = { a, transferDetails ->
            abstractWorldSecureOperation.pre(a, transferDetails)
                && transferDetails.from in world.authPurses.dom
                && transferDetails.to in world.authPurses.dom
                && world.authPurses[transferDetails.from].balance >= transferDetails.value
        },
        post = { a, transferDetails, result: World ->
            abstractWorldSecureOperation.post(a, transferDetails, result)
                && result.authPurses[transferDetails.from].balance == world.authPurses[transferDetails.from].balance
                && result.authPurses[transferDetails.to].lost == world.authPurses[transferDetails.to].lost + transferDetails.value
                && result.authPurses[transferDetails.to] == world.authPurses[transferDetails.to]
        }
    )
}
