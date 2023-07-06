package com.anaplan.engineering.azuki.graphs.adapter.jung.execution

import com.google.common.graph.Graph
import com.google.common.graph.MutableGraph


class ExecutionEnvironment {

    private val graphs = mutableMapOf<String, MutableGraph<*>>()

    fun <V> act(graphName: String, action: MutableGraph<V>.() -> Unit) =
        (graphs[graphName] as? MutableGraph<V>)?.action() ?: throw ExecutionException("No such graph $graphName")

    fun <T> get(graphName: String, get: Graph<*>.() -> T): T =
        graphs[graphName]?.get() ?: throw ExecutionException("No such graph $graphName")

    fun addGraph(graphName: String, graph: MutableGraph<*>) {
        if (graphs.containsKey(graphName)) throw ExecutionException("Graph $graphName already exists")
        graphs[graphName] = graph
    }
}

class ExecutionException(msg: String) : RuntimeException(msg)
