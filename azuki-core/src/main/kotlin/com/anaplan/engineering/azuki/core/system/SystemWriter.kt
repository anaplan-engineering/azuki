package com.anaplan.engineering.azuki.core.system

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.util.*

interface SystemWriter<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory
    > {

    val actionFactory: AF
    val checkFactory: CF
    val queryFactory: QF
    val actionGeneratorFactory: AGF

    fun write(systemDefinition: SystemDefinition, context: String? = null)

    companion object {
        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> locateScenarioWriter(): SystemWriter<AF, CF, QF, AGF>? {
            val loader = ServiceLoader.load(SystemWriter::class.java)
            val scenarioWriters =
                loader.iterator().asSequence().filterIsInstance<SystemWriter<AF, CF, QF, AGF>>().toList()
            return if (scenarioWriters.isEmpty()) {
                null
            } else {
                if (scenarioWriters.size > 1) {
                    Log.warn("More than one scenario writer on class path choosing ${scenarioWriters.first().javaClass} arbitrarily")
                }
                scenarioWriters.first()
            }
        }

        private val Log = LoggerFactory.getLogger(SystemWriter::class.java)
    }

}

class DefaultImplementationSystemWriter<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory
    >(private val implementation: Implementation<AF, CF, QF, AGF, *>) : SystemWriter<AF, CF, QF, AGF> {

    private val systemFactory = implementation.createSystemFactory()

    override val actionFactory = systemFactory.actionFactory
    override val checkFactory by lazy { (systemFactory as? VerifiableSystemFactory)!!.checkFactory }
    override val queryFactory by lazy { (systemFactory as? QueryableSystemFactory)!!.queryFactory }
    override val actionGeneratorFactory by lazy { (systemFactory as? ActionGeneratingSystemFactory)!!.actionGeneratorFactory }

    override fun write(
        systemDefinition: SystemDefinition,
        context: String?
    ) {
        if (Log.isDebugEnabled) {
            File.createTempFile(context ?: "system", ".sys").apply {
                Log.debug("Writing system to $this")
                val sb = StringBuilder().apply {
                    appendSection("Declarations", systemDefinition.declarations)
                    appendSection("Commands", systemDefinition.commands)
                    appendSection("Checks", systemDefinition.checks)
                    appendSection("Forall Queries", systemDefinition.forAllQueries)
                    appendSection("Queries", systemDefinition.queries)
                    appendSection("Action generators", systemDefinition.actionGenerators)
                    appendSection("Regardless of", systemDefinition.regardlessOfActions)
                }
                writeText(sb.toString())
            }
        }
    }

    private fun StringBuilder.appendSection(sectionName: String, objects: List<Any>) {
        if (objects.isNotEmpty()) {
            append("$sectionName: \n")
            append(objects.joinToString("") { " * ${it.javaClass.simpleName}\n" })
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(DefaultImplementationSystemWriter::class.java)
    }

}
