package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.DerivedQuery

fun interface ScriptGenerationDerivedQuery<T> : DerivedQuery<T> {

    fun getDerivedQueryScript(): String

    /**
     * As `getDerivedQueryScript`, but returns a renderable script element instead of a string.
     *
     * By default, this returns an element that wraps the contents of `getDerivedQueryScript`.
     * Override this for more control over the script generation of a derived query.
     * (If doing so, consider making `getDerivedQueryScript` call the `render` method of the script element for
     * consistency.)
     */
    fun getDerivedQueryScriptElement(): ScriptElement = ScriptStringFragment(getDerivedQueryScript())
}
