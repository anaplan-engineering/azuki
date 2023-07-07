package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck

interface GraphCheckFactory: CheckFactory {
    fun hasVertexCount(graphName: String, count: Long): Check = UnsupportedCheck
    fun hasShortestPath(graphName: String, from: Any, to: Any, shortestPath: List<Any>): Check = UnsupportedCheck
}
