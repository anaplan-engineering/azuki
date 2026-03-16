package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.ActionGenerator

fun interface ScriptGenerationActionGenerator: ActionGenerator {

    fun getActionGeneratorScript(): String

    /**
     * As `getActionGeneratorScript`, but returns a renderable script element instead of a string.
     *
     * By default, this returns an element that wraps the contents of `getActionGeneratorScript`.
     * Override this for more control over the script generation of an action.
     * (If doing so, consider making `getActionGeneratorScript` call the `render` method of the script element for
     * consistency.)
     */
    fun getActionGeneratorScriptElement(): ScriptElement = ScriptStringFragment(getActionGeneratorScript())
}
