package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api

import com.anaplan.engineering.azuki.core.system.FunctionalElement
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldFunctionalElements.Transfer

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IsA(
    val functionalElement: FunctionalElement
)

object IntraWorldFunctionalElements {
    const val Purse = 1
    const val World = 2
    const val Transfer = 3
}

object MondexFunctions {

    @IsA(Transfer)
    const val AbortedTransfer = 4
}
