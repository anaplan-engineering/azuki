package com.anaplan.engineering.azuki.core.parser

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import java.util.concurrent.locks.ReentrantLock
import javax.script.ScriptEngineManager

object SimpleScenarioParser: ScenarioParser<BuildableScenario<*>> {

    private val engine by lazy {
        ScriptEngineManager().getEngineByExtension("kts")
    }

    private val lock = ReentrantLock()

    override fun parse(
        scenarioString: String,
        requiredImports: String
    ): BuildableScenario<*> {
        val script = """
            $requiredImports

            $scenarioString
        """
        // KotlinJsr223JvmLocalScriptEngine is not thread safe
        val evalResult = try {
            lock.lock()
            engine.eval(script)
        } finally {
            lock.unlock()
        }
        if (evalResult !is BuildableScenario<*>) {
            throw IllegalArgumentException("Script does not evaluate to scenario")
        }
        return evalResult
    }

}
