package com.anaplan.engineering.azuki.examples.mondex.specification

import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld
import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Module

@Module(makeable = false)
interface WorldSystem {
    val world: ConWorld

    @FunctionProvider(WorldSystemFunctions::class)
    val functions: WorldSystemFunctions
}

abstract class WorldSystemFunctions

