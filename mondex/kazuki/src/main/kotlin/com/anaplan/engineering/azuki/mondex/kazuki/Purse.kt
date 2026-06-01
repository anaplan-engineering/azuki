package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

@Module
interface Purse {
    val balance: nat
    val lost: nat
}
