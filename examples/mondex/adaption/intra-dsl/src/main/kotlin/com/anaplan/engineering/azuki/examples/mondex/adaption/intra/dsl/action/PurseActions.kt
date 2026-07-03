package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.action

interface PurseActions {

    // all-in-one action to create a transfer and proceed from start to finish
    fun makeATransfer(fromPurse: String, toPurse: String, value: Int)


}
