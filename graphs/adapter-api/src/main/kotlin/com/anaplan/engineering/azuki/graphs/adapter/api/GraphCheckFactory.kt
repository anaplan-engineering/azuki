package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck

interface GraphCheckFactory : CheckFactory {
    fun hasVertexCount(graphName: String, count: Long): Check = UnsupportedCheck
    fun hasShortestPath(graphName: String, path: List<Any>): Check = UnsupportedCheck
    fun hasCycles(graphName: String, hasCycle: Boolean): Check = UnsupportedCheck
    fun hasSimpleCycleCount(graphName: String, count: Long): Check = UnsupportedCheck
    fun pathExists(graphName: String, from: Any, to: Any, result: Boolean): Check = UnsupportedCheck
}
