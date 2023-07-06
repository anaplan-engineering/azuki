package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedAction

interface GraphActionFactory : ActionFactory {

    fun create(graphName: String): Action = UnsupportedAction
    fun <T> addVertex(graphName: String, vertex: T): Action = UnsupportedAction
    fun <T> addEdge(graphName: String, source: T, target: T): Action = UnsupportedAction

}
