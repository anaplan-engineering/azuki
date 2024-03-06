package com.anaplan.engineering.azuki.core.system

import java.io.File

interface SystemFactory<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults, S: System<AF, CF>> {

    fun create(systemDefinition: SystemDefinition): S

    val actionFactory: AF
}

interface VerifiableSystemFactory<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults, S:VerifiableSystem<AF, CF>> :
    SystemFactory<AF, CF, QF, AGF, SD, S> {
    val checkFactory: CF
}

interface QueryableSystemFactory<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults, S: QueryableSystem<AF, CF>> :
    SystemFactory<AF, CF, QF, AGF, SD, S> {
    val queryFactory: QF
}

interface ActionGeneratingSystemFactory<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults, S: ActionGeneratingSystem<AF, CF>> :
    SystemFactory<AF, CF, QF, AGF, SD, S> {
    val actionGeneratorFactory: AGF
}

interface ReportGeneratingSystemFactory<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults, S: ReportGeneratingSystem<AF, CF>> :
    VerifiableSystemFactory<AF, CF, QF, AGF, SD, S>

interface PersistableSystemFactory<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, SD : SystemDefaults, S: PersistableSystem<AF, CF>> :
    VerifiableSystemFactory<AF, CF, QF, AGF, SD, S> {
}

interface System<AF : ActionFactory, CF : CheckFactory>

interface VerifiableSystem<AF : ActionFactory, CF : CheckFactory> : System<AF, CF> {
    fun verify(): VerificationResult
}

interface QueryableSystem<AF : ActionFactory, CF : CheckFactory> : System<AF, CF> {
    fun query(): List<Answer<*, CF>>
}

interface ActionGeneratingSystem<AF : ActionFactory, CF : CheckFactory> : System<AF, CF> {
    fun generateActions(): List<(AF) -> Action>
}

interface ReportGeneratingSystem<AF : ActionFactory, CF : CheckFactory> : VerifiableSystem<AF, CF> {
    fun generateReport(name: String): File
}

interface MutableSystem<AF : ActionFactory, CF : CheckFactory> : System<AF, CF> {
    fun applyIteration(systemIteration: SystemIteration)

    fun destroy()
}

interface PersistableSystem<AF : ActionFactory, CF : CheckFactory> : VerifiableSystem<AF, CF> {

    /**
     * Note that the file returned would not typically contain the serialized system, but would instead provide the
     * necessary information that would enable the verifying implementation to deserialize the system.
     */
    fun verifyAndSerialize(): VerificationResult

    fun deserializeAndVerify(file: File): VerificationResult

}


data class SystemIteration(
    val commands: List<Action>,
    val checks: List<Check> = emptyList(),
    val regardlessOfActions: List<List<Action>> = emptyList(),
    val queries: List<Query<*>> = emptyList(),
    val forAllQueries: List<DerivedQuery<*>> = emptyList(),
    val actionGenerators: List<ActionGenerator> = emptyList(),
)

data class SystemDefinition(
    val declarations: List<Action>,
    val commands: List<Action> = emptyList(),
    val checks: List<Check> = emptyList(),
    val regardlessOfActions: List<List<Action>> = emptyList(),
    val queries: List<Query<*>> = emptyList(),
    val forAllQueries: List<DerivedQuery<*>> = emptyList(),
    val actionGenerators: List<ActionGenerator> = emptyList(),
)

sealed class VerificationResult {
    // The system was valid, the checks were run, and they were satisfied
    open class Verified(
        // val coverage
    ) : VerificationResult()

    class VerifiedAndSerialized(val file: File) : Verified()

    // The system was valid and the checks were run, but they were not satisfied
    class Unverified : VerificationResult()

    // The system given was not valid
    class SystemInvalid(
        val cause: Exception
    ) : VerificationResult()
}
