package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.kazuki.core.*

@Module
interface BetweenSystem {

    val betweenWorld: BetweenWorld

    @FunctionProvider(BetweenSystemFunctions::class)
    val functions: BetweenSystemFunctions
}

class BetweenSystemFunctions(val betweenSystem: BetweenSystem) {

}
