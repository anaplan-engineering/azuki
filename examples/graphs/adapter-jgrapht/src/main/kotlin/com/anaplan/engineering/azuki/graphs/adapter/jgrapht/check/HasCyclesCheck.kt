package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedCheckException
import com.anaplan.engineering.azuki.graphs.adapter.api.HasCyclesBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.alg.cycle.CycleDetector

class HasCyclesCheck(
    private val graphName: String,
    private val hasCycles: Boolean
) : JGraphTCheck, HasCyclesBehaviour() {

    override fun check(env: ExecutionEnvironment) =
        checkEqual(hasCycles, env.get<String, Boolean>(graphName) {
            if (type.isDirected) {
                CycleDetector(this).detectCycles()
            } else {
                throw LateDetectUnsupportedCheckException("JGraphT can only check if a graph has cycles when graph is directed")
            }
        })

}
