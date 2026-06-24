package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory

interface MondexCheckFactory : CheckFactory {
    val purse: PurseCheckFactory
    val world: WorldCheckFactory
}

interface PurseCheckFactory {
    fun purseExists(personName: String, result: Boolean): Check
    fun purseExists(personName: String, balance: ULong, lost: ULong, result: Boolean): Check
}

interface WorldCheckFactory {
    fun worldExists(authPurses: Map<String, Purse>, result: Boolean): Check
    fun noValueCreation(result: Boolean): Check
    fun allValueAccounted(result: Boolean): Check
}
