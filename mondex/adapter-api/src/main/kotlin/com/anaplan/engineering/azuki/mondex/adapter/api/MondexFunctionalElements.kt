package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.system.FunctionalElement
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexFunctionalElements.WorldFunction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IsA(
    val functionalElement: FunctionalElement
)

object MondexFunctionalElements {
    const val Purse = 1
    const val World = 2
    const val WorldFunction = 3
}

object MondexFunctions {
    @IsA(WorldFunction)
    const val Transfer = 4

    @IsA(WorldFunction)
    const val Ignore = 5
}
