package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution

import org.jgrapht.Graph

class ExecutionEnvironment {

    private val graphs = mutableMapOf<String, Graph<*, *>>()

    fun <V> act(graphName: String, action: Graph<V, *>.() -> Unit) =
        (graphs[graphName] as? Graph<V, *>)?.action() ?: throw ExecutionException("No such graph $graphName")

    fun <V, T> get(graphName: String, get: Graph<V, *>.() -> T): T =
        (graphs[graphName] as? Graph<V, *>)?.get() ?: throw ExecutionException("No such graph $graphName")

    fun addGraph(graphName: String, graph: Graph<*, *>) {
        if (graphs.containsKey(graphName)) throw ExecutionException("Graph $graphName already exists")
        graphs[graphName] = graph
    }
}

class ExecutionException(msg: String) : RuntimeException(msg)
