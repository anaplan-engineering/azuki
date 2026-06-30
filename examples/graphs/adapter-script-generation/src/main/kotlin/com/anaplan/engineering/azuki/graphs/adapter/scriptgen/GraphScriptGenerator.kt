package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationService
import com.anaplan.engineering.azuki.script.generation.ScriptingHelper

val GraphScriptGeneration = ScriptGenerationService.new(GraphScriptGenerationActionFactory,
    GraphScriptGenerationCheckFactory,
    ::GraphDeclarationState).build()

val GraphScriptingHelper = ScriptingHelper(mapOf(
    String::class to { v: Any? -> "\"${v.toString()}\"" },
    Long::class to { v: Any? -> v.toString() },
))
