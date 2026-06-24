package com.anaplan.engineering.azuki.mondex.dsl.check

import com.anaplan.engineering.azuki.mondex.dsl.PurseCheckBlock
import com.anaplan.engineering.azuki.mondex.dsl.WorldCheckBlock

interface MondexChecks {
    fun purseExists(personName: String)
    fun purseExists(personName: String, balance: Int, lost: Int)
    fun purseOf(personName: String, init: PurseCheckBlock.() -> Unit)
    fun worldExists(init: WorldCheckBlock.() -> Unit)
    fun noValueCreation()
    fun allValueAccounted()
}
