package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ParallelActionFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedAction

interface GraphActionFactory<A : Action> : ActionFactory, ParallelActionFactory<A> {

    val undirected: UndirectedGraphActionFactory
    val directed: DirectedGraphActionFactory
}

interface UndirectedGraphActionFactory {

    fun create(graphName: String): Action = UnsupportedAction
    fun <T> addVertex(graphName: String, vertex: T): Action = UnsupportedAction
    fun <T> addEdge(graphName: String, source: T, target: T): Action = UnsupportedAction

}

interface DirectedGraphActionFactory {

    fun create(graphName: String): Action = UnsupportedAction
    fun <T> addVertex(graphName: String, vertex: T): Action = UnsupportedAction
    fun <T> addEdge(graphName: String, source: T, target: T): Action = UnsupportedAction

}
