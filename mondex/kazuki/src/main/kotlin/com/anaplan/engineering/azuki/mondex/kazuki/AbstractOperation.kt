package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

@Module
interface AbstractOperation {
    val world: Delta<World>
    val aQ: AIN
    val aE: AOUT

    @Invariant
    fun outputAlwaysANullOut() = aE == aNullOut
}
