package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ParallelAction
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
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationAction

object GraphScriptGenActionFactory : GraphActionFactory<ScriptGenerationAction> {

    override val undirected: UndirectedGraphActionFactory = UndirectedGraphScriptGenActionFactory

    override val directed: DirectedGraphActionFactory = DirectedGraphScriptGenActionFactory

    override fun createParallelAction(actions: List<List<Action>>): ParallelAction<ScriptGenerationAction> =
        ScriptGenParallelAction(actions.map { it.map(toScriptGenAction) })
}

internal fun Action.toScriptGenAction() =
    this as? ScriptGenerationAction ?: throw IllegalArgumentException("Incompatible action: $this")

internal val toScriptGenAction: (Action) -> ScriptGenerationAction = { it.toScriptGenAction() }

private class ScriptGenParallelAction(actions: List<List<ScriptGenerationAction>>) :
    ParallelAction<ScriptGenerationAction>(actions),
    ScriptGenerationAction {

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

private object UndirectedGraphScriptGenActionFactory : UndirectedGraphActionFactory {

    private class AddEdgeToUndirectedGraphScriptGenAction<V>(graphName: String, source: V, target: V) :
        AddEdgeToUndirectedGraphDeclarableAction<V>(graphName, source, target), ScriptGenerationAction {
        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(UndirectedGraphActions::addEdgeToUndirectedGraph,
                graphName,
                source,
                target)
    }

    private class AddVertexToUndirectedGraphScriptGenAction<V>(graphName: String, vertex: V) :
        AddVertexToUndirectedGraphDeclarableAction<V>(graphName, vertex), ScriptGenerationAction {
        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(UndirectedGraphActions::addVertexToUndirectedGraph,
                graphName,
                vertex)
    }

    private class CreateUndirectedGraphScriptGenAction(graphName: String) :
        CreateUndirectedGraphDeclarableAction(graphName), ScriptGenerationAction {
        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(UndirectedGraphActions::createUndirected, graphName)
    }

    override fun create(graphName: String): ScriptGenerationAction = CreateUndirectedGraphScriptGenAction(graphName)

    override fun <V> addEdge(graphName: String, source: V, target: V): ScriptGenerationAction =
        AddEdgeToUndirectedGraphScriptGenAction(graphName, source, target)

    override fun <V> addVertex(graphName: String, vertex: V): ScriptGenerationAction =
        AddVertexToUndirectedGraphScriptGenAction(graphName, vertex)
}

private object DirectedGraphScriptGenActionFactory : DirectedGraphActionFactory {

    private class CreateDirectedGraphScriptGenAction(graphName: String) :
        CreateUndirectedGraphDeclarableAction(graphName), ScriptGenerationAction {
        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(DirectedGraphActions::createDirected, graphName)
    }

    private class AddEdgeToDirectedGraphScriptGenAction<V>(graphName: String, source: V, target: V) :
        AddEdgeToDirectedGraphDeclarableAction<V>(graphName, source, target), ScriptGenerationAction {
        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(DirectedGraphActions::addEdgeToDirectedGraph,
                graphName,
                source,
                target)
    }

    private class AddVertexToDirectedGraphScriptGenAction<V>(graphName: String, vertex: V) :
        AddVertexToDirectedGraphDeclarableAction<V>(graphName, vertex), ScriptGenerationAction {
        override fun getActionScript() =
            GraphScriptingHelper.scriptifyFunction(DirectedGraphActions::addVertexToDirectedGraph, graphName, vertex)
    }

    override fun create(graphName: String): ScriptGenerationAction =
        CreateDirectedGraphScriptGenAction(graphName)

    override fun <V> addEdge(graphName: String, source: V, target: V): ScriptGenerationAction =
        AddEdgeToDirectedGraphScriptGenAction(graphName, source, target)

    override fun <V> addVertex(graphName: String, vertex: V): ScriptGenerationAction =
        AddVertexToDirectedGraphScriptGenAction(graphName, vertex)

}
