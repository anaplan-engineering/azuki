package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

typealias Name = String

@Module
interface TransferDetails {
    val from: Name
    val to: Name
    val value: nat
}
