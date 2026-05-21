package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

@Module
interface TransferDetails {
    val from: Name
    val to: Name
    val value: nat
}
