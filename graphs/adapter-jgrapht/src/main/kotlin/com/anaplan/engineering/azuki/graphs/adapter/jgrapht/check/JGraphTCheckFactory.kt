package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class JGraphTCheckFactory: GraphCheckFactory {

    override fun hasVertexCount(graphName: String, count: Long) = HasVertexCountCheck(graphName, count)
}

interface JGraphTCheck: Check {
    fun check(env: ExecutionEnvironment) : Boolean
}


val toJGraphTCheck: (Check) -> JGraphTCheck = {
    @Suppress("UNCHECKED_CAST")
    it as? JGraphTCheck ?: throw IllegalArgumentException("Invalid check: $it")
}
