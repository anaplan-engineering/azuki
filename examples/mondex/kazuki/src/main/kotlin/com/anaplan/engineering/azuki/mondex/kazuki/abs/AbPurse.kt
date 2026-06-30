package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.nat

@Module
interface AbPurse {
    val balance: nat
    val lost: nat
}
