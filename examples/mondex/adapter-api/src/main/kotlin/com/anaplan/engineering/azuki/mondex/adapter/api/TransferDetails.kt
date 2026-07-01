package com.anaplan.engineering.azuki.mondex.adapter.api

data class TransferDetails(
    val from: Name,
    val to: Name,
    val value: ULong
)

data class PayDetails(
    val td: TransferDetails,
    val fromSeqNo: ULong,
    val toSeqNo: ULong,
)

// Using builders to allow for delegate construction on the DSL
// This will hide the internal difference between Abstract / Concrete world transaction inputs

// Abstract world transactions expects a TransferDetails
class TransferDetailsBuilder {
    lateinit var from: Name
    lateinit var to: Name
    var value: ULong = 0UL

    fun build(): TransferDetails =
        TransferDetails(
            from = from,
            to = to,
            value = value
        )
}

// Concrete world transactions expects a PayDetails
class PayDetailsBuilder {
    lateinit var from: Name
    lateinit var to: Name
    var value: ULong = 0UL
    var fromSeqNo: ULong = 0UL
    var toSeqNo: ULong = 0UL

    fun build(): PayDetails =
        PayDetails(
            TransferDetails(
                from = from,
                to = to,
                value = value
            ),
            fromSeqNo,
            toSeqNo
        )
}
