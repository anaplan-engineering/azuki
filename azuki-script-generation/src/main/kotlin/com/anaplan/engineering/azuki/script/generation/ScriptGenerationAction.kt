package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ParallelAction

interface ScriptGenerationAction<in E: ScriptGenerationEnvironment> : Action {

    fun getActionScript(environment: E): String
}

interface NoEnvScriptGenerationAction : ScriptGenerationAction<NoScriptGenerationEnvironment> {

    fun getActionScript(): String

    override fun getActionScript(environment: NoScriptGenerationEnvironment) = getActionScript()
}

class ScriptGenerationParallelAction<in E: ScriptGenerationEnvironment>(actions: List<List<ScriptGenerationAction<E>>>) :
    ParallelAction<ScriptGenerationAction<E>>(actions), ScriptGenerationAction<E> {

    override fun getActionScript(environment: E) = """
        parallel(${
        runActionsSequentially { it.getActionScript(environment) }.joinToString(", ") {
            """
            {
                ${it.joinToString("\n")}
            }
            """
        }
    })
    """
}

@Suppress("UNCHECKED_CAST")
fun <E: ScriptGenerationEnvironment> Action.toScriptGenAction() =
    this as? ScriptGenerationAction<E> ?: throw IllegalArgumentException("Incompatible action: $this")
