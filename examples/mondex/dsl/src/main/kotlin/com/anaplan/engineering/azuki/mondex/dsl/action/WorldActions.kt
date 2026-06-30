package com.anaplan.engineering.azuki.mondex.dsl.action

interface WorldActions : WorldDeclarableActions {
//    fun createWorld(authPurses: Map<String, Pair<ULong, ULong>>)
}

interface WorldDeclarableActions {
    fun thereIsATransfer(fromPurse: String, toPurse: String, value: Int, succeed: Boolean = true)
    fun thereIsNoTransfer()
}
