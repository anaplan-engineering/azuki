package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.CreateGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.dsl.action.GraphActions
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationAction

object GraphScriptGenActionFactory : GraphActionFactory {

    override fun create(graphName: String): ScriptGenerationAction = CreateGraphScriptGenAction(graphName)

    override fun <V> addEdge(graphName: String, source: V, target: V): ScriptGenerationAction =
        AddEdgeScriptGenAction(graphName, source, target)

    override fun <V> addVertex(graphName: String, vertex: V): ScriptGenerationAction =
        AddVertexScriptGenAction(graphName, vertex)

    private class CreateGraphScriptGenAction(graphName: String) :
        CreateGraphDeclarableAction(graphName), ScriptGenerationAction {
        override fun getActionScript() = GraphScriptingHelper.scriptifyFunction(GraphActions::create, graphName)
    }

    private class AddEdgeScriptGenAction<V>(graphName: String, source: V, target: V) :
        AddEdgeDeclarableAction<V>(graphName, source, target), ScriptGenerationAction {
        override fun getActionScript() = GraphScriptingHelper.scriptifyFunction(GraphActions::addEdge, source, target)
    }

    private class AddVertexScriptGenAction<V>(graphName: String, vertex: V) :
        AddVertexDeclarableAction<V>(graphName, vertex), ScriptGenerationAction {
        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(GraphActions::addVertex, graphName, vertex)
    }
}
