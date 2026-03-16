package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ParallelAction

interface ScriptGenerationAction<in E : ScriptGenerationEnvironment> : Action {

    fun getActionScript(environment: E): String

    /**
     * As `getActionScript`, but returns a renderable script element instead of a string.
     *
     * By default, this returns an element that wraps the contents of `getActionScript`.
     * Override this for more control over the script generation of an action.
     * (If doing so, consider making `getActionScript` call the `render` method of the script element for consistency.)
     */
    fun getActionScriptElement(environment: E): ScriptElement = ScriptStringFragment(getActionScript(environment))
}

class ScriptGenerationParallelAction<in E : ScriptGenerationEnvironment>(actions: List<List<ScriptGenerationAction<E>>>) :
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
fun <E : ScriptGenerationEnvironment> Action.toScriptGenAction() =
    this as? ScriptGenerationAction<E> ?: throw IllegalArgumentException("Incompatible action: $this")
