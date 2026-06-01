package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

@Module
interface GlobalWorld {
    val gAuthPurses: Mapping<Name, Purse>
}
