package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbPurse
import com.anaplan.engineering.kazuki.core.*

@Module
interface World {
    val purses: Mapping<Name, Purse>
}
