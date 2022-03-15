package com.anaplan.engineering.azuki.core.system

import java.util.*

interface Implementation<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults> {

    /**
     * The name of this implementation
     */
    val name: String

    /**
     * Does this implementation provide a total implementation of the abstract modelling API (i.e. no unsupported
     * actions/checks).
     */
    val total: Boolean

    /**
     * The system defaults preferred by this implementation
     */
    val implementationDefaults: SD

    fun createSystemFactory(systemDefaults: SD = implementationDefaults): SystemFactory<AF, CF, QF, AGF, SD>

    companion object {
        fun locateImplementations(): List<Implementation<*, *, *, *, *>> {
            val restrictTo = java.lang.System.getProperty("implementations")?.split(",")?.map { it.trim() }
            val loader = ServiceLoader.load(Implementation::class.java)
            val factories =
                loader.iterator().asSequence().filter { restrictTo == null || restrictTo.contains(it.name) }.toList()
            if (factories.isEmpty()) throw IllegalStateException("No implementations found")
            return factories
        }
    }
}

annotation class ImplementationVersion(
    val name: String,
    val version: String,
)
