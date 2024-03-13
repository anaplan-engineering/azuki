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

    fun createSystemFactory(systemDefaults: SD = implementationDefaults): SystemFactory<AF, CF, QF, AGF, SD, *>

    val versionFilter: VersionFilter

    companion object {
        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> locateImplementations(): List<Implementation<AF, CF, QF, AGF, *>> {
            val loader = ServiceLoader.load(Implementation::class.java)
            return loader.iterator().asSequence().filterIsInstance<Implementation<AF, CF, QF, AGF, *>>().toList()
        }
    }

    interface VersionFilter {

        /**
         * Provides a means for an implementation to skip verification of scenario based on version it was introduced
         *
         * @param scenarioVersion the version of the implementation associated with the scenario
         * @return true if this instance of the implementation can verify a scenario with the given properties
         */
        fun canVerify(scenarioVersion: String?): Boolean

        object DefaultVersionFilter : VersionFilter {
            override fun canVerify(scenarioVersion: String?) = true
        }
    }
}

annotation class ImplementationVersion(
    val name: String,
    val version: String,
)
