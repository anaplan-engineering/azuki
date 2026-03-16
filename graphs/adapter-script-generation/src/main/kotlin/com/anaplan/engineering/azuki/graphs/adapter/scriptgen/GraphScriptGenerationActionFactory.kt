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

object GraphScriptGenerationActionFactory : GraphActionFactory<GraphScriptGenerationAction> {

    override val undirected: UndirectedGraphActionFactory = UndirectedGraphScriptGenActionFactory

    override val directed: DirectedGraphActionFactory = DirectedGraphScriptGenActionFactory

    override fun createParallelAction(actions: List<List<Action>>): ScriptGenerationParallelAction<NoScriptGenerationEnvironment> =
        ScriptGenerationParallelAction(actions.map { it.map(Action::toScriptGenAction) })
}

typealias GraphScriptGenerationAction = ScriptGenerationAction<NoScriptGenerationEnvironment>

interface GraphScriptElementGenerationAction : GraphScriptGenerationAction {

    override fun getActionScript(environment: NoScriptGenerationEnvironment) =
        getActionScriptElement(environment).render(RenderContext())

    abstract override fun getActionScriptElement(environment: NoScriptGenerationEnvironment): ScriptElement
}

private object UndirectedGraphScriptGenActionFactory : UndirectedGraphActionFactory {

    private class AddEdgeToUndirectedGraphScriptGenerationAction<V>(graphName: String, source: V, target: V) :
        AddEdgeToUndirectedGraphDeclarableAction<V>(graphName, source, target), GraphScriptElementGenerationAction {

        override fun getActionScriptElement(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunctionAsElement(UndirectedGraphActions::addEdgeToUndirectedGraph,
                graphName,
                source,
                target)
    }

    private class AddVertexToUndirectedGraphScriptGenerationAction<V>(graphName: String, vertex: V) :
        AddVertexToUndirectedGraphDeclarableAction<V>(graphName, vertex), GraphScriptElementGenerationAction {

        override fun getActionScriptElement(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunctionAsElement(UndirectedGraphActions::addVertexToUndirectedGraph,
                graphName,
                vertex)
    }

    private class CreateUndirectedGraphScriptGenerationAction(graphName: String) :
        CreateUndirectedGraphDeclarableAction(graphName), GraphScriptElementGenerationAction {

        override fun getActionScriptElement(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunctionAsElement(UndirectedGraphActions::createUndirected, graphName)
    }

    override fun create(graphName: String): GraphScriptGenerationAction =
        CreateUndirectedGraphScriptGenerationAction(graphName)

    override fun <V> addEdge(graphName: String, source: V, target: V): GraphScriptElementGenerationAction =
        AddEdgeToUndirectedGraphScriptGenerationAction(graphName, source, target)

    override fun <V> addVertex(graphName: String, vertex: V): GraphScriptElementGenerationAction =
        AddVertexToUndirectedGraphScriptGenerationAction(graphName, vertex)
}

private object DirectedGraphScriptGenActionFactory : DirectedGraphActionFactory {

    private class CreateDirectedGraphScriptGenerationAction(graphName: String) :
        CreateUndirectedGraphDeclarableAction(graphName), GraphScriptElementGenerationAction {

        override fun getActionScriptElement(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunctionAsElement(DirectedGraphActions::createDirected, graphName)
    }

    private class AddEdgeToDirectedGraphScriptGenerationAction<V>(graphName: String, source: V, target: V) :
        AddEdgeToDirectedGraphDeclarableAction<V>(graphName, source, target), GraphScriptElementGenerationAction {

        override fun getActionScriptElement(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunctionAsElement(DirectedGraphActions::addEdgeToDirectedGraph,
                graphName,
                source,
                target)
    }

    private class AddVertexToDirectedGraphScriptGenerationAction<V>(graphName: String, vertex: V) :
        AddVertexToDirectedGraphDeclarableAction<V>(graphName, vertex), GraphScriptElementGenerationAction {

        override fun getActionScriptElement(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunctionAsElement(DirectedGraphActions::addVertexToDirectedGraph,
                graphName,
                vertex)
    }

    override fun create(graphName: String): GraphScriptGenerationAction =
        CreateDirectedGraphScriptGenerationAction(graphName)

    override fun <V> addEdge(graphName: String, source: V, target: V): GraphScriptGenerationAction =
        AddEdgeToDirectedGraphScriptGenerationAction(graphName, source, target)

    override fun <V> addVertex(graphName: String, vertex: V): GraphScriptGenerationAction =
        AddVertexToDirectedGraphScriptGenerationAction(graphName, vertex)
}
