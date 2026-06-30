package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.graphs.adapter.api.GetVertexCountBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class HasVertexCountCheck(private val graphName: String, private val count: Long) : JGraphTCheck,
    GetVertexCountBehaviour() {

    override fun check(env: ExecutionEnvironment) =
        checkEqual(count, env.get<Any, Long>(graphName) {
            vertexSet().size.toLong()
        })

}
