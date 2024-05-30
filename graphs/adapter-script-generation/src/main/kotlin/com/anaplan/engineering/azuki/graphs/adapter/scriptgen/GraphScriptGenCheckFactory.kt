package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.adapter.api.GetCycleCountBehavior
import com.anaplan.engineering.azuki.graphs.adapter.api.GetVertexCountBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.HasCyclesBehavior
import com.anaplan.engineering.azuki.graphs.dsl.check.GraphChecks
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationCheck

object GraphScriptGenCheckFactory : GraphCheckFactory {

    override fun hasVertexCount(graphName: String, count: Long): ScriptGenerationCheck =
        HasVertexCountCheck(graphName, count)

    override fun hasShortestPath(
        graphName: String,
        from: Any,
        to: Any,
        shortestPath: List<Any>
    ): ScriptGenerationCheck = HasShortestPathCheck(graphName, from, to, shortestPath)

    override fun hasCycles(
        graphName: String,
        hasCycles: Boolean,
    ): ScriptGenerationCheck = HasCyclesCheck(graphName, hasCycles)

    override fun hasSimpleCycleCount(
        graphName: String,
        count: Long,
    ): ScriptGenerationCheck = HasSimpleCycleCountCheck(graphName, count)

    private class HasVertexCountCheck(private val graphName: String, private val count: Long) :
        GetVertexCountBehaviour(), ScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasVertexCount, graphName, count)
    }

    private class HasShortestPathCheck(
        private val graphName: String,
        private val from: Any,
        private val to: Any,
        private val shortestPath: List<Any>
    ) :
        GetVertexCountBehaviour(), ScriptGenerationCheck {
        override fun getCheckScript() =
            GraphScriptingHelper.scriptifyFunction(GraphChecks::hasVertexCount, graphName, from, to, shortestPath)
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

}
