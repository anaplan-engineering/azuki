package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.api.*
import com.anaplan.engineering.azuki.graphs.dsl.check.GraphChecks
import com.anaplan.engineering.azuki.script.generation.BasicScriptGenerationCheck

object GraphScriptGenerationCheckFactory : GraphCheckFactory {

    override fun hasVertexCount(graphName: String, count: Long): BasicScriptGenerationCheck =
        HasVertexCountCheck(graphName, count)

    override fun hasShortestPath(
        graphName: String,
        path: List<Any>,
    ): BasicScriptGenerationCheck = HasShortestPathCheck(graphName, path)

    override fun hasCycles(
        graphName: String,
        hasCycles: Boolean,
    ): BasicScriptGenerationCheck = HasCyclesCheck(graphName, hasCycles)

    override fun hasSimpleCycleCount(
        graphName: String,
        count: Long,
    ): BasicScriptGenerationCheck = HasSimpleCycleCountCheck(graphName, count)

    override fun pathExists(
        graphName: String,
        from: Any,
        to: Any,
        result: Boolean
    ): BasicScriptGenerationCheck = PathExistsCheck(graphName, from, to, result)

    override fun hasEdgeCount(
        graphName: String,
        count: Long
    ): BasicScriptGenerationCheck = HasEdgeCount(graphName, count)

    private class HasVertexCountCheck(private val graphName: String, private val count: Long) :
        GetVertexCountBehaviour(), BasicScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasVertexCount, graphName, count)
    }

    private class HasShortestPathCheck(
        private val graphName: String,
        private val path: List<Any>,
    ) :
        GetShortestPathBehaviour(), BasicScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasShortestPath, graphName, path)
    }

    private class HasCyclesCheck(
        private val graphName: String,
        private val hasCycles: Boolean,
    ) :
        HasCyclesBehaviour(), BasicScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasCycles, graphName, hasCycles)
    }

    private class HasSimpleCycleCountCheck(
        private val graphName: String,
        private val count: Long
    ) :
        GetCycleCountBehaviour(), BasicScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasSimpleCycleCount, graphName, count)
    }

    private class PathExistsCheck(
        private val graphName: String,
        private val from: Any,
        private val to: Any,
        private val result: Boolean,
    ) :
        PathExistsBehaviour(), BasicScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::pathExists, graphName, from, to, result)
    }

    private class HasEdgeCount(
        private val graphName: String,
        private val count: Long
    ) :
        GetEdgeCountBehaviour(), BasicScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasEdgeCount, graphName, count)
    }
}
