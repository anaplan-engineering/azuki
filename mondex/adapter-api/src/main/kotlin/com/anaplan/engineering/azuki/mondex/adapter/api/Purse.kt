package com.anaplan.engineering.azuki.mondex.adapter.api

//LF @QST is this better / what's the difference?
//sealed class Purse2 {
//    data class Ab(val balance: ULong, val lost: ULong) : Purse2()
//    data class Con(val balance: ULong, val exLog: Set<PayDetails>, val name: String, val nextSeqNo: ULong, val pdAuth: PayDetails) : Purse2()
//}
// we need a design here that allows for abstraction between purse types perhaps a sealed interface?
/*sealed*/ interface Purse {
   // val balance: ULong
}

// No need for this anymore
//data class AbPurse(
//    override val balance: ULong,
//    val lost: ULong,
//) : Purse
//
//data class ConPurse(
//    override val balance: ULong,
//    val exLog: Set<PayDetails>,
//    val name: Name,
//    val nextSeqNo: ULong,
//    val pdAuth: PayDetails?,
//    val status: Status = Status.eaFrom
//) : Purse

enum class Status { eaFrom, eaTo, epr, epv, epa }
