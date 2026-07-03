package com.anaplan.engineering.azuki.examples.mondex.specification.abstract

import com.anaplan.engineering.kazuki.core.*

sealed interface AIn
object aNullIn: AIn

@Module
interface Transfer: AIn {
    val transferDetails: TransferDetails
}

sealed interface AOut
object aNullOut: AOut

