package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.DirectedGraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.UndirectedGraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeToDirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeToUndirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexToDirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexToUndirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.CreateUndirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.dsl.action.DirectedGraphActions
import com.anaplan.engineering.azuki.graphs.dsl.action.UndirectedGraphActions
import com.anaplan.engineering.azuki.script.generation.*

object GraphScriptGenActionFactory : GraphActionFactory<ScriptGenerationAction<NoScriptGenerationEnvironment>> {

    override val undirected: UndirectedGraphActionFactory = UndirectedGraphScriptGenActionFactory

    override val directed: DirectedGraphActionFactory = DirectedGraphScriptGenActionFactory

    override fun createParallelAction(actions: List<List<Action>>) =
        ScriptGenerationParallelAction(actions.map { it.map(Action::toScriptGenAction) })
}

private object UndirectedGraphScriptGenActionFactory : UndirectedGraphActionFactory {

    private class AddEdgeToUndirectedGraphScriptGenerationAction<V>(graphName: String, source: V, target: V) :
        AddEdgeToUndirectedGraphDeclarableAction<V>(graphName, source, target), NoEnvScriptGenerationAction {

        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(UndirectedGraphActions::addEdgeToUndirectedGraph,
                graphName,
                source,
                target)
    }

    private class AddVertexToUndirectedGraphScriptGenerationAction<V>(graphName: String, vertex: V) :
        AddVertexToUndirectedGraphDeclarableAction<V>(graphName, vertex), NoEnvScriptGenerationAction {

        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(UndirectedGraphActions::addVertexToUndirectedGraph,
                graphName,
                vertex)
    }

    private class CreateUndirectedGraphScriptGenerationAction(graphName: String) :
        CreateUndirectedGraphDeclarableAction(graphName), NoEnvScriptGenerationAction {

        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(UndirectedGraphActions::createUndirected, graphName)
    }

    override fun create(graphName: String): NoEnvScriptGenerationAction = CreateUndirectedGraphScriptGenerationAction(graphName)

    override fun <V> addEdge(graphName: String, source: V, target: V): NoEnvScriptGenerationAction =
        AddEdgeToUndirectedGraphScriptGenerationAction(graphName, source, target)

    override fun <V> addVertex(graphName: String, vertex: V): NoEnvScriptGenerationAction =
        AddVertexToUndirectedGraphScriptGenerationAction(graphName, vertex)
}

private object DirectedGraphScriptGenActionFactory : DirectedGraphActionFactory {

    private class CreateDirectedGraphScriptGenerationAction(graphName: String) :

        CreateUndirectedGraphDeclarableAction(graphName), NoEnvScriptGenerationAction {
        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(DirectedGraphActions::createDirected, graphName)
    }

    private class AddEdgeToDirectedGraphScriptGenerationAction<V>(graphName: String, source: V, target: V) :
        AddEdgeToDirectedGraphDeclarableAction<V>(graphName, source, target), NoEnvScriptGenerationAction {

        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(DirectedGraphActions::addEdgeToDirectedGraph,
                graphName,
                source,
                target)
    }

    private class AddVertexToDirectedGraphScriptGenerationAction<V>(graphName: String, vertex: V) :
        AddVertexToDirectedGraphDeclarableAction<V>(graphName, vertex), NoEnvScriptGenerationAction {

        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(DirectedGraphActions::addVertexToDirectedGraph, graphName, vertex)
    }

    override fun create(graphName: String): NoEnvScriptGenerationAction =
        CreateDirectedGraphScriptGenerationAction(graphName)

    override fun <V> addEdge(graphName: String, source: V, target: V): NoEnvScriptGenerationAction =
        AddEdgeToDirectedGraphScriptGenerationAction(graphName, source, target)

    override fun <V> addVertex(graphName: String, vertex: V): NoEnvScriptGenerationAction =
        AddVertexToDirectedGraphScriptGenerationAction(graphName, vertex)
}
