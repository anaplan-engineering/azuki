package com.anaplan.engineering.azuki.examples.mondex.specification.abstract

import com.anaplan.engineering.kazuki.core.function

object SecurityProperties {

    val noValueCreated = function(
        command = { before: AbWorld, after: AbWorld ->
            after.properties.totalBalance <= before.properties.totalBalance
        }
    )

    val allValueAccounted = function(
        command = { before: AbWorld, after: AbWorld ->
            after.properties.totalValue == before.properties.totalValue
        }
    )

    val authentic = function(
        command = { world: AbWorld, name: Name -> name in world.abAuthPurse.dom }
    )

    val sufficientFunds = function(
        command = { world: AbWorld, td: TransferDetails -> td.value <= world.abAuthPurse[td.from].balance }
    )

}
