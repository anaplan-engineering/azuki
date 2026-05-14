package com.anaplan.engineering.azuki.core.system

import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

interface SystemWriter<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory
    > {

    /*
     * If non-null, the below factories will be used to create the system definition provided to the write function.
     * If null, the execution factories will be used.
     */
    val actionFactory: AF? get() = null
    val checkFactory: CF? get() = null
    val queryFactory: QF? get() = null
    val actionGeneratorFactory: AGF? get() = null

    fun write(systemDefinition: SystemDefinition, context: String? = null)

    companion object {
        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> locateSystemWriter(): SystemWriter<AF, CF, QF, AGF>? {
            val loader = ServiceLoader.load(SystemWriter::class.java)
            val systemWriters =
                loader.iterator().asSequence().filterIsInstance<SystemWriter<AF, CF, QF, AGF>>().toList()
            Log.debug("Located system writers: ${systemWriters.joinToString(", ") { it::class.simpleName!! }}")
            return if (systemWriters.isEmpty()) {
                null
            } else {
                if (systemWriters.size > 1) {
                    Log.warn("More than one scenario writer on class path choosing ${systemWriters.first().javaClass} arbitrarily")
                }
                systemWriters.first()
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
    > : SystemWriter<AF, CF, QF, AGF> {

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

class NoSystemWriter<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> :
    SystemWriter<AF, CF, QF, AGF>
{

    override fun write(systemDefinition: SystemDefinition, context: String?) {
        // do nothing
    }
}
