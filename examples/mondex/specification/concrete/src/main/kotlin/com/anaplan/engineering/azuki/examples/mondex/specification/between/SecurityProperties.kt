package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.kazuki.core.function

object SecurityProperties {

    val noValueCreated = function(
        command = { before: BetweenWorld, after: BetweenWorld ->
            after.properties.totalBalance <= before.properties.totalBalance
        }
    )

    val allValueAccounted = function(
        command = { before: BetweenWorld, after: BetweenWorld ->
            after.properties.totalValue == before.properties.totalValue
        }
    )

    val authentic = function(
        command = { world: BetweenWorld, name: Name -> name in world.conAuthPurse.dom }
    )

    val sufficientFunds = function(
        command = { world: BetweenWorld, td: TransferDetails -> td.value <= world.conAuthPurse[td.from].balance }
    )

}
