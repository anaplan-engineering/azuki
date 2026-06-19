package com.anaplan.engineering.azuki.mondex.adapter.api

data class TransferDetails(
    val fromPurse: String,
    val toPurse: String,
    val value: ULong
)
