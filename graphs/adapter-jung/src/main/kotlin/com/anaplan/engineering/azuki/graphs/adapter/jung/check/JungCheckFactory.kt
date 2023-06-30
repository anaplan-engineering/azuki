package com.anaplan.engineering.azuki.graphs.adapter.jung.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class JungCheckFactory: GraphCheckFactory {

    override fun hasVertexCount(graphName: String, count: Long) = HasVertexCountCheck(graphName, count)
}

interface JungCheck: Check {
    fun check(env: ExecutionEnvironment) : Boolean
}


val toJungCheck: (Check) -> JungCheck = {
    @Suppress("UNCHECKED_CAST")
    it as? JungCheck ?: throw IllegalArgumentException("Invalid check: $it")
}
