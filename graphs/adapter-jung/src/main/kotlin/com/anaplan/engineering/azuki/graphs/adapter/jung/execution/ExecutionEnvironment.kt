package com.anaplan.engineering.azuki.graphs.adapter.jung.execution

import com.google.common.graph.Graph


class ExecutionEnvironment {

    private val graphs = mutableMapOf<String, Graph<*>>()

    fun act(graphName: String, action: Graph<*>.() -> Unit) =
        graphs[graphName]?.action() ?: throw ExecutionException("No such graph $graphName")

    fun <T> get(graphName: String, get: Graph<*>.() -> T): T =
        graphs[graphName]?.get() ?: throw ExecutionException("No such graph $graphName")

    fun addGraph(graphName: String, graph: Graph<*>) {
        if (graphs.containsKey(graphName)) throw ExecutionException("Graph $graphName already exists")
        graphs[graphName] = graph
    }
}

class ExecutionException(msg: String) : RuntimeException(msg)
