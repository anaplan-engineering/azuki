package com.anaplan.engineering.azuki.core.system

import java.io.File

interface SystemFactory<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults> {

    fun create(systemDefinition: SystemDefinition): System<AF, CF>

    val actionFactory: AF

    val checkFactory: CF

    val queryFactory: QF

    val actionGeneratorFactory: AGF
}

data class SystemDefinition(
    val declarations: List<Action>,
    val actions: List<Action>,
    val checks: List<Check> = emptyList(),
    val regardlessOfActions: List<List<Action>> = emptyList(),
    val queries: List<Query<*>> = emptyList(),
    val forAllQueries: List<DerivedQuery<*>> = emptyList(),
    val actionGenerators: List<ActionGenerator> = emptyList(),
)

interface System<AF : ActionFactory, CF : CheckFactory> {

    val supportedActions: Set<SystemAction>

    fun verify(): VerificationResult

    fun query(): List<Answer<*, CF>>

    fun generateActions(): List<(AF) -> Action>

    fun generateReport(name: String): File

    enum class SystemAction {
        Query,
        Verify,
        Report,
        GenerateActions,
    }

}

sealed class VerificationResult {
    // The system was valid, the checks were run, and they were satisfied
    class Verified(
        // val coverage
    ) : VerificationResult()

    // The system was valid and the checks were run, but they were not satisfied
    class Unverified : VerificationResult()

    // The system given was not valid
    class SystemInvalid(
        val cause: Exception
    ) : VerificationResult()
}


interface Query<T> : ReifiedBehavior

interface ActionGenerator

interface DerivedQuery<T>

interface Answer<T, CF : CheckFactory> {
    val to: Query<T>
    val value: T
    fun createChecks(factory: CF): List<Check>
}

interface QueryFactory
interface ActionGeneratorFactory

object NoQueryFactory : QueryFactory
object NoActionGeneratorFactory : ActionGeneratorFactory
