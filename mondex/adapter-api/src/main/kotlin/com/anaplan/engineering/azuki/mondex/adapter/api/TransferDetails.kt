package com.anaplan.engineering.azuki.mondex.adapter.api

data class TransferDetails(
    val fromPurse: Name,
    val toPurse: Name,
    val value: ULong
)

data class PayDetails(
    //LF @QST not sure whether to include this as a field or through inheritance; data class inheritance issues
    val td: TransferDetails,
    val fromSeqNo: ULong,
    val toSeqNo: ULong,
)
