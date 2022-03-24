package com.anaplan.engineering.azuki.core.system

import org.slf4j.LoggerFactory
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
        private val Log = LoggerFactory.getLogger(Implementation::class.java)

        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> locateImplementations(): List<Implementation<AF, CF, QF, AGF, *>> {
            val loader = ServiceLoader.load(Implementation::class.java)
            val implementations = loader.iterator().asSequence().map {
                @Suppress("UNCHECKED_CAST")
                it as Implementation<AF, CF, QF, AGF, *>
            }.toList()
            Log.debug("Located implementations: $implementations")
            if (implementations.isEmpty()) throw IllegalStateException("No implementations found")
            return implementations
        }
    }
}

annotation class ImplementationVersion(
    val name: String,
    val version: String,
)
