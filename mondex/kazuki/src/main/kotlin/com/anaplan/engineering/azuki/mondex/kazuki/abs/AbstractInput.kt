package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

sealed interface AbstractInput
object abstractNullInput: AbstractInput

@Module
interface Transfer: AbstractInput {
    val transferDetails: TransferDetails
}
