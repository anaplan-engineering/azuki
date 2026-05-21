package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

typealias Name = String

sealed interface AIN
object aNullIn: AIN
class transfer(val transferDetails: TransferDetails): AIN

sealed interface AOUT
object aNullOut: AOUT

@Module
interface World {
    val authPurses: Mapping<Name, Purse>
}
