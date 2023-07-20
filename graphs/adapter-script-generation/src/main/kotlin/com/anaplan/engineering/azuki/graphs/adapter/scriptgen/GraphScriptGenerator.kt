package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState
import com.anaplan.engineering.azuki.script.generation.ScriptGenerator
import com.anaplan.engineering.azuki.script.generation.ScriptingHelper

object GraphScriptGenerator :
    ScriptGenerator<GraphActionFactory, GraphCheckFactory, NoQueryFactory, NoActionGeneratorFactory, GraphDeclarationState>(
        GraphScriptGenActionFactory,
        GraphScriptGenCheckFactory,
        GraphDeclarationState.Factory,
    ) {


}


val GraphScriptingHelper = ScriptingHelper(mapOf(
    String::class to { v: Any? -> "\"${v.toString()}\"" },
    Long::class to { v: Any? -> v.toString() },
))
