package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.Then
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexCheckFactory

class MondexThen(private val checkFactory: MondexCheckFactory) : Then<MondexCheckFactory> {

    private val checkList = mutableListOf<Check>()

    override fun checks() = checkList
}
