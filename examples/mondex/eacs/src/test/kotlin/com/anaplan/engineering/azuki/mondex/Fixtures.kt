package com.anaplan.engineering.azuki.mondex

import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.TransferDetails

const val person1 = "person1"
const val person2 = "person2"
const val person3 = "person3"

val pd1 = PayDetails(TransferDetails(person1, person2, 10U), 0U, 0U)
val pd2 = PayDetails(TransferDetails(person1, person2, 10U), 0U, 0U)
