package com.anaplan.engineering.azuki.mondex.kazuki.betw

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Set1
import com.anaplan.engineering.kazuki.core.nat

@Module
interface PayDetails : TransferDetails {
    val fromSeqNo: nat
    val toSeqNo: nat

    @Invariant
    fun namesDistinct() = from != to
}

@Module
interface CounterPartyDetails {
    val name: Name
    val value: nat
    val nextSeqNo: nat
}

sealed interface Clear
//LF: Injective projection for Clear over a non-empty set of pay details
class image(val pd1: Set1<PayDetails>): Clear

