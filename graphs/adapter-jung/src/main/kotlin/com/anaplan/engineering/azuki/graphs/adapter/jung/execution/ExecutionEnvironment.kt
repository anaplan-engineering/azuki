package com.anaplan.engineering.azuki.graphs.adapter.jung.execution

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network


class ExecutionEnvironment {

    private val graphs = mutableMapOf<String, MutableNetwork<*, Pair<*, *>>>()

    fun <V> act(graphName: String, action: MutableNetwork<V, Pair<V, V>>.() -> Unit) =
        (graphs[graphName] as? MutableNetwork<V, Pair<V, V>>)?.action() ?: throw ExecutionException("No such graph $graphName")

    fun <V, T> get(graphName: String, get: Network<V, Pair<V, V>>.() -> T): T =
        (graphs[graphName] as? Network<V, Pair<V, V>>)?.get() ?: throw ExecutionException("No such graph $graphName")

    fun addGraph(graphName: String, graph: MutableNetwork<*, Pair<*, *>>) {
        if (graphs.containsKey(graphName)) throw ExecutionException("Graph $graphName already exists")
        graphs[graphName] = graph
    }
}

class ExecutionException(msg: String) : RuntimeException(msg)
