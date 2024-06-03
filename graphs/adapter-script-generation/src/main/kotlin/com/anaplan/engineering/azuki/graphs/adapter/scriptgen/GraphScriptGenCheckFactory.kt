package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.api.*
import com.anaplan.engineering.azuki.graphs.dsl.check.GraphChecks
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationCheck

object GraphScriptGenCheckFactory : GraphCheckFactory {

    override fun hasVertexCount(graphName: String, count: Long): ScriptGenerationCheck =
        HasVertexCountCheck(graphName, count)

    override fun hasShortestPath(
        graphName: String,
        path: List<Any>,
    ): ScriptGenerationCheck = HasShortestPathCheck(graphName, path)

    override fun hasCycles(
        graphName: String,
        hasCycles: Boolean,
    ): ScriptGenerationCheck = HasCyclesCheck(graphName, hasCycles)

    override fun hasSimpleCycleCount(
        graphName: String,
        count: Long,
    ): ScriptGenerationCheck = HasSimpleCycleCountCheck(graphName, count)

    override fun pathExists(
        graphName: String,
        from: Any,
        to: Any,
        result: Boolean
    ): ScriptGenerationCheck = PathExistsCheck(graphName, from, to, result)

    private class HasVertexCountCheck(private val graphName: String, private val count: Long) :
        GetVertexCountBehaviour(), ScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasVertexCount, graphName, count)
    }

    private class HasShortestPathCheck(
        private val graphName: String,
        private val path: List<Any>,
    ) :
        GetShortestPathBehaviour(), ScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasShortestPath, graphName, path)
    }

    private class HasCyclesCheck(
        private val graphName: String,
        private val hasCycles: Boolean,
    ) :
        HasCyclesBehavior(), ScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasCycles, graphName, hasCycles)
    }

    private class HasSimpleCycleCountCheck(
        private val graphName: String,
        private val count: Long
    ) :
        GetCycleCountBehavior(), ScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasSimpleCycleCount, graphName, count)
    }

    private class PathExistsCheck(
        private val graphName: String,
        private val from: Any,
        private val to: Any,
        private val result: Boolean,
    ) :
        PathExistsBehaviour(), ScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::pathExists, graphName, from, to, result)
    }
}
