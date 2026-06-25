package com.anaplan.engineering.azuki.mondex.kazuki.abs

import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails
import com.anaplan.engineering.kazuki.core.*

// Keep the Z names as much as possible for clarity
sealed interface AIn
object aNullIn: AIn

//LF This shouldn't/doesn't need to be a module?
class transfer(val transferDetails: TransferDetails): AIn
//@Module
//interface Transfer: AIn {
//    val transferDetails: TransferDetails
//}

sealed interface AOut
object aNullOut: AOut

