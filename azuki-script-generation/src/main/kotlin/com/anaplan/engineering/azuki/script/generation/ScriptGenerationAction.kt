package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ParallelAction

interface ScriptGenerationAction : Action {
    fun getActionScript(): String
}

class ScriptGenerationParallelAction(actions: List<List<ScriptGenerationAction>>) :
    ParallelAction<ScriptGenerationAction>(actions), ScriptGenerationAction {

    override fun getActionScript() = """
       parallel(
        ${
        runActionsSequentially { it.getActionScript() }.joinToString(", ") {
            """ {
                    ${it.joinToString("\n")}
                }
            """
        }
    }
       )
    """
}

fun Action.toScriptGenAction() =
    this as? ScriptGenerationAction ?: throw IllegalArgumentException("Incompatible action: $this")
