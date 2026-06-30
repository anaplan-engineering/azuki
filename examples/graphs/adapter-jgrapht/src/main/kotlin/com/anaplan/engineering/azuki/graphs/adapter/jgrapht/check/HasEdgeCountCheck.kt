package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.graphs.adapter.api.GetEdgeCountBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class HasEdgeCountCheck(private val graphName: String, private val count: Long) : JGraphTCheck,
    GetEdgeCountBehaviour() {

    override fun check(env: ExecutionEnvironment) =
        checkEqual(count, env.get<Any, Long>(graphName) {
            edgeSet().size.toLong()
        })

}
