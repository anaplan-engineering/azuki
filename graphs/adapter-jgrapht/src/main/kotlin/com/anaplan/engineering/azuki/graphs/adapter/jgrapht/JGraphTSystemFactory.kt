package com.anaplan.engineering.azuki.graphs.adapter.jgrapht

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action.JGraphTAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action.JGraphTActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action.toJGraphTAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check.JGraphTCheck
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check.JGraphTCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check.toJGraphTCheck
import java.io.File

class JGraphTSystemFactory : SystemFactory<
    GraphActionFactory,
    GraphCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    NoSystemDefaults
    > {
    override fun create(systemDefinition: SystemDefinition): System<GraphActionFactory, GraphCheckFactory> {
        return JGraphTSystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.actions.map(toJGraphTAction),
            systemDefinition.checks.map(toJGraphTCheck),
        )
    }

    override val actionFactory = JGraphTActionFactory()
    override val checkFactory = JGraphTCheckFactory()
    override val queryFactory = NoQueryFactory
    override val actionGeneratorFactory = NoActionGeneratorFactory

}

data class JGraphTSystem(
    val declarableActions: List<DeclarableAction>,
    val buildActions: List<JGraphTAction>,
    val checks: List<JGraphTCheck>
) : System<GraphActionFactory, GraphCheckFactory> {

    override val supportedActions = if (checks.isNotEmpty()) {
        setOf(System.SystemAction.Verify)
    } else {
        setOf()
    }

    override fun verify(): VerificationResult {
        TODO("Not yet implemented")
    }

    override fun generateReport(name: String) = throw UnsupportedOperationException()

    override fun query() = throw UnsupportedOperationException()

    override fun generateActions() = throw UnsupportedOperationException()
}


