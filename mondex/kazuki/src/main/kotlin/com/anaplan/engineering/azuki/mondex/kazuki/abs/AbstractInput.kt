package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails
import com.anaplan.engineering.kazuki.core.*

sealed interface AbstractInput
object abstractNullInput: AbstractInput

@Module
interface Transfer: AbstractInput {
    val transferDetails: TransferDetails
}
