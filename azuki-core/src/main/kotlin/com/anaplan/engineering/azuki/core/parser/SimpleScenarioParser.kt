package com.anaplan.engineering.azuki.core.parser

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

open class SimpleScenarioParser<out S : BuildableScenario<*>> : ScenarioParser<S> {
    private val engine by lazy {
        BasicJvmScriptingHost()
    }

    private val lock = ReentrantLock()

    /**
     * Imports that are pulled into every scenario in addition to those specified in the call to `parse`.
     */
    protected open val defaultImports: ScenarioParsingContext.() -> Unit = {}

    @Deprecated("Passing required imports as a string is deprecated and may disappear in a future major revision",
        replaceWith = ReplaceWith("parse(scenarioString) { requireImportsFromString(requiredImports) }",
            "com.anaplan.engineering.azuki.core.parser.ScenarioParser",
            "com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext"))
    final override fun parse(
        scenarioString: String, requiredImports: String
    ): S = parse(scenarioString) {
        requireImportsFromString(requiredImports)
    }

    final override fun parse(
        scenarioString: String,
        initContext: ScenarioParsingContext.() -> Unit
    ): S {
        val script = scenarioString.toScriptSource()
        val scenarioContext = ScenarioParsingContext().apply(defaultImports).apply(initContext)

        // TODO: do we need this lock for the Kotlin host?
        val evalResult = try {
            lock.lock()
            engine.evalWithTemplate<SimpleScenario>(script, {
                defaultImports(scenarioContext.imports)
            })
        } finally {
            lock.unlock()
        }

        when (val result = evalResult.valueOrThrow().returnValue) {
            is ResultValue.Unit -> {
                Log.error("Unit returned by the following script:\n\n{}", scenarioString)

                throw IllegalArgumentException("Script does not evaluate to scenario: it returned nothing")
            }
            is ResultValue.Error -> {
                Log.error("Exception thrown in the following script:\n\n{}", scenarioString)

                throw IllegalArgumentException("Script threw an exception while evaluating: $result", result.error)
            }
            else -> {
                if (result !is ResultValue.Value || result.value !is BuildableScenario<*>) {
                    Log.error("Non-scenario value returned by the following script:\n\n{}", scenarioString)

                    throw IllegalArgumentException("Script does not evaluate to scenario: got $result")
                }
                @Suppress("UNCHECKED_CAST") return result.value as S
            }
        }
    }

    companion object {
        val Log = LoggerFactory.getLogger(SimpleScenarioParser::class.java)
    }
}

@KotlinScript(fileExtension = "scn", compilationConfiguration = SimpleScenarioCompilationConfiguration::class)
abstract class SimpleScenario

object SimpleScenarioCompilationConfiguration : ScriptCompilationConfiguration({
    jvm {
        // Extract the whole classpath from context classloader and use it as dependencies
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
}) {
    private fun readResolve(): Any = SimpleScenarioCompilationConfiguration
}
