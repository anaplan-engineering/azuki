package com.anaplan.engineering.azuki.examples.mondex.specification.abstract

import com.anaplan.engineering.kazuki.core.*

@Module
interface AbPurse {
    val balance: nat
    val lost: nat
}
