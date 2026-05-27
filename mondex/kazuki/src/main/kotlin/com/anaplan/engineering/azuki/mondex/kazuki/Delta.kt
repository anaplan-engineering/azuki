package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

@Module
interface Delta<T> {
    val old: T
    val dash: T
}
