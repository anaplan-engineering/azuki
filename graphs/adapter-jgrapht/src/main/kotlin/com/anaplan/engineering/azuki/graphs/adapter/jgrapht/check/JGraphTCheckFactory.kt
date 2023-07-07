package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.slf4j.LoggerFactory

class JGraphTCheckFactory : GraphCheckFactory {

    override fun hasVertexCount(graphName: String, count: Long) = HasVertexCountCheck(graphName, count)

    override fun hasShortestPath(graphName: String, from: Any, to: Any, shortestPath: List<Any>) =
        HasShortestPathCheck(graphName, from, to, shortestPath)
}

interface JGraphTCheck : Check {
    fun check(env: ExecutionEnvironment): Boolean

    fun checkEqual(expected: Any, actual: Any): Boolean {
        val equal = expected == actual
        if (!equal) {
            Log.error("Eauality check failed: expected=$expected actual=$actual")
        }
        return equal
    }

    companion object {
        private val Log = LoggerFactory.getLogger(JGraphTCheck::class.java)
    }
}


val toJGraphTCheck: (Check) -> JGraphTCheck = {
    @Suppress("UNCHECKED_CAST")
    it as? JGraphTCheck ?: throw IllegalArgumentException("Invalid check: $it")
}
