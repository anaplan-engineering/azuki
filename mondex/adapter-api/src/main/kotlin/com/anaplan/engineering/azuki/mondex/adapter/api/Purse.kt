package com.anaplan.engineering.azuki.mondex.adapter.api

//LF: @QST: is this better / what's the difference?
//sealed class Purse2 {
//    data class Ab(val balance: ULong, val lost: ULong) : Purse2()
//    data class Con(val balance: ULong, val exLog: Set<PayDetails>, val name: String, val nextSeqNo: ULong, val pdAuth: PayDetails) : Purse2()
//}

//LF: @QST: we need a design here that allows for abstraction between purse types perhaps a sealed interface?
sealed interface Purse {
    val balance: ULong
}

data class AbPurse(
    override val balance: ULong,
    val lost: ULong,
) : Purse

data class ConPurse(
    override val balance: ULong,
    val exLog: Set<PayDetails>,
    //LF: ConWorld invariant says the name matches the mapping the purse is in
    val name: Name,
    val nextSeqNo: ULong,
    //LF: ConPurse invariant says the name must be in from or to. This represents the "last" payment done by this purse
    //    To bootstrap (first purse), you might need to have here something that might be null, given you can't have a
    //    payment to yourself. Or allow only when status = eaFrom?
    val pdAuth: PayDetails?,
    val status: Status = Status.eaFrom
) : Purse

enum class Status { eaFrom, eaTo, epr, epv, epa }
