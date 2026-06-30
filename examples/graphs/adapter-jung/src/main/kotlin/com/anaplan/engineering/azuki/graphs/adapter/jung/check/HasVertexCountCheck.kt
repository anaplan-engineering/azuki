package com.anaplan.engineering.azuki.graphs.adapter.jung.check

import com.anaplan.engineering.azuki.graphs.adapter.api.GetVertexCountBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class HasVertexCountCheck(private val graphName: String, private val count: Long) : JungCheck, GetVertexCountBehaviour() {

    override fun check(env: ExecutionEnvironment): Boolean {
        val actual = env.get<Any, Long>(graphName) {
            nodes().size.toLong()
        }
        return count == actual
    }


}
