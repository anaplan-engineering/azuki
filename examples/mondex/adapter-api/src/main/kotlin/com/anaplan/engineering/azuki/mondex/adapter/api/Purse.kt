package com.anaplan.engineering.azuki.mondex.adapter.api

sealed interface Purse {
   val balance: ULong
}

data class AbPurse(
    override val balance: ULong,
    val lost: ULong,
) : Purse

// Payment details of current transaction to be given upon protocol start
// Useful to enable separation between creation of purses and starting of protocol runs
data class ConPurse(
    override val balance: ULong,
    val exLog: Set<PayDetails>,
    val name: Name,
    val nextSeqNo: ULong,
    val pdAuth: PayDetails?,
    val status: Status
) : Purse

enum class Status { eaFrom, eaTo, epr, epv, epa }
