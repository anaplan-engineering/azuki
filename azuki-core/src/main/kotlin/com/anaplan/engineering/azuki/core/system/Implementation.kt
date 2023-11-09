package com.anaplan.engineering.azuki.core.system

import java.util.*

interface Implementation<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults> {

    /**
     * The name of this implementation
     */
    val name: String

    /**
     * The system defaults preferred by this implementation
     */
    val implementationDefaults: SD

    fun createSystemFactory(systemDefaults: SD = implementationDefaults): SystemFactory<AF, CF, QF, AGF, SD>

    companion object {
        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> locateImplementations(): List<Implementation<AF, CF, QF, AGF, *>> {
            val loader = ServiceLoader.load(Implementation::class.java)
            return loader.iterator().asSequence().filterIsInstance<Implementation<AF, CF, QF, AGF, *>>().toList()
        }
    }
}

annotation class ImplementationVersion(
    val name: String,
    val version: String,
)
