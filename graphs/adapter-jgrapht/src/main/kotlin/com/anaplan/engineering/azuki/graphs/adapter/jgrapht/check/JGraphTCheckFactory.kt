package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory

class JGraphTCheckFactory: GraphCheckFactory {

}

interface JGraphTCheck: Check


val toJGraphTCheck: (Check) -> JGraphTCheck = {
    @Suppress("UNCHECKED_CAST")
    it as? JGraphTCheck ?: throw IllegalArgumentException("Invalid check: $it")
}
