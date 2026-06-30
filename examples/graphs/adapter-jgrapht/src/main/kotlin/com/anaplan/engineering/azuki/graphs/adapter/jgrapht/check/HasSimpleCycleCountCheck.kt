package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedCheckException
import com.anaplan.engineering.azuki.graphs.adapter.api.GetCycleCountBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles

class HasSimpleCycleCountCheck(
    private val graphName: String,
    private val result: Long
) : JGraphTCheck, GetCycleCountBehaviour() {

    override fun check(env: ExecutionEnvironment) =
        checkEqual(result, env.get<String, Long>(graphName) {
            if (type.isDirected) {
                HawickJamesSimpleCycles(this).countSimpleCycles()
            } else {
                throw LateDetectUnsupportedCheckException("JGraphT can only check if a graph has cycles when graph is directed")
            }
        })
}


