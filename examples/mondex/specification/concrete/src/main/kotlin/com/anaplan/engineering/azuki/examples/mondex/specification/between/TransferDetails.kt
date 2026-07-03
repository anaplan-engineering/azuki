package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.kazuki.core.*

@Module
interface TransferDetails {
    val from: Name
    val to: Name
    val value: nat
}

