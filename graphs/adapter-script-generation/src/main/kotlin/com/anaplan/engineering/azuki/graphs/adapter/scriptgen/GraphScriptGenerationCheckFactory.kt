package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.api.*
import com.anaplan.engineering.azuki.graphs.dsl.check.GraphChecks
import com.anaplan.engineering.azuki.script.generation.NoScriptGenerationEnvironment
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationCheck

typealias GraphScriptGenerationCheck = ScriptGenerationCheck<NoScriptGenerationEnvironment>

object GraphScriptGenerationCheckFactory : GraphCheckFactory {

    override fun hasVertexCount(graphName: String, count: Long): GraphScriptGenerationCheck =
        HasVertexCountCheck(graphName, count)

    override fun hasShortestPath(
        graphName: String,
        path: List<Any>,
    ): GraphScriptGenerationCheck = HasShortestPathCheck(graphName, path)

    override fun hasCycles(
        graphName: String,
        hasCycles: Boolean,
    ): GraphScriptGenerationCheck = HasCyclesCheck(graphName, hasCycles)

    override fun hasSimpleCycleCount(
        graphName: String,
        count: Long,
    ): GraphScriptGenerationCheck = HasSimpleCycleCountCheck(graphName, count)

    override fun pathExists(
        graphName: String,
        from: Any,
        to: Any,
        result: Boolean
    ): GraphScriptGenerationCheck = PathExistsCheck(graphName, from, to, result)

    override fun hasEdgeCount(
        graphName: String,
        count: Long
    ): GraphScriptGenerationCheck = HasEdgeCount(graphName, count)

    private class HasVertexCountCheck(private val graphName: String, private val count: Long) :
        GetVertexCountBehaviour(), GraphScriptGenerationCheck {
        override fun getCheckScript(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasVertexCount, graphName, count)
    }

    private class HasShortestPathCheck(
        private val graphName: String,
        private val path: List<Any>,
    ) :
        GetShortestPathBehaviour(), GraphScriptGenerationCheck {
        override fun getCheckScript(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasShortestPath, graphName, path)
    }

    private class HasCyclesCheck(
        private val graphName: String,
        private val hasCycles: Boolean,
    ) :
        HasCyclesBehaviour(), GraphScriptGenerationCheck {
        override fun getCheckScript(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasCycles, graphName, hasCycles)
    }

    private class HasSimpleCycleCountCheck(
        private val graphName: String,
        private val count: Long
    ) :
        GetCycleCountBehaviour(), GraphScriptGenerationCheck {
        override fun getCheckScript(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasSimpleCycleCount, graphName, count)
    }

    private class PathExistsCheck(
        private val graphName: String,
        private val from: Any,
        private val to: Any,
        private val result: Boolean,
    ) :
        PathExistsBehaviour(), GraphScriptGenerationCheck {
        override fun getCheckScript(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::pathExists, graphName, from, to, result)
    }

    private class HasEdgeCount(
        private val graphName: String,
        private val count: Long
    ) :
        GetEdgeCountBehaviour(), GraphScriptGenerationCheck {
        override fun getCheckScript(environment: NoScriptGenerationEnvironment) =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasEdgeCount, graphName, count)
    }
}
