package com.anaplan.engineering.azuki.mondex

import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.TransferRef

const val world1 = "world1"

const val person1 = "person1"
const val person2 = "person2"
const val person3 = "person3"
const val person4 = "person4"

const val transfer1 = "transfer1"
const val transfer2 = "transfer2"

val transfer1Ref = TransferRef(transfer1, person1, person2, 2)

const val SUCCESS = true
const val FAILURE = !SUCCESS
