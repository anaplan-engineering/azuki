package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.kazuki.core.*

@Module
interface CounterPartyDetails {
    val name: Name
    val value: nat
    val nextSeqNo: nat
}
