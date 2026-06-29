package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.azuki.mondex.kazuki.Purse
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.nat

@Module
interface AbPurse : Purse {
    val lost: nat
}
