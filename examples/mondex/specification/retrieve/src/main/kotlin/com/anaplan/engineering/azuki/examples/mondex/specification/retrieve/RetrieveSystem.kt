package com.anaplan.engineering.azuki.examples.mondex.specification.retrieve

import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConSystem
import com.anaplan.engineering.kazuki.core.*

@Module
interface RetrieveSystem {
    // Not sure if we should keep all three and/or whether system or world is better
    val abSystem: AbSystem
    val betSystem: BetweenSystem
    val conSystem: ConSystem

    // e.g.
    @Invariant
    fun abstractBetweenNames() = abSystem.abWorld.abAuthPurse.dom == betSystem.world.conAuthPurse.dom

    @Invariant
    fun betweenConcreteNames() = abSystem.abWorld.abAuthPurse.dom == conSystem.world.conAuthPurse.dom


    @FunctionProvider(RetrieveSystemFunctions::class)
    val functions: RetrieveSystemFunctions
}


class RetrieveSystemFunctions(val retrieveSystem: RetrieveSystem) {

    /**
     * e.g do transfer updates abstract & concrete and then invariant ensure retrieve holds at system relation
     *   + posts at function level can enforce function-local retrieves
     */


}

