package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.action

//LF QST: in intra-api has this as a transfer (not a purse) action; guess doesn't matter; but better align? Also for createPurse
interface PurseActions {

    // all-in-one action to create a transfer and proceed from start to finish
    fun makeATransfer(fromPurse: String, toPurse: String, value: Int, successful: Boolean)


}
