package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.script.generation.*
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationService
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

val RightOfWayScriptGeneration = ScriptGenerationService.new(RightOfWayScriptGenerationActionFactory,
    RightOfWayScriptGenerationCheckFactory,
    ::RightOfWayDeclarationState).withEnvironmentFactory(::RightOfWayGenerationEnvironment)
    .withActionGeneratorFactory(RightOfWayScriptGenerationActionGeneratorFactory)
    .withQueryFactory(RightOfWayScriptGenerationQueryQueryFactory)
    .withVerifyFactory(RightOfWayScriptGenerationVerificationQueryFactory)
    .build()

// None of the declaration builders for RightOfWay use the environment:
typealias RightOfWayScriptGenerationDeclarationBuilder<D> = ScriptGenerationDeclarationBuilder<RightOfWayGenerationEnvironment, D>
typealias RightOfWayScriptGenerationDeclarationBuilderFactory<D> = ScriptGenerationDeclarationBuilderFactory<RightOfWayGenerationEnvironment, D>

internal const val Width = 3
internal const val Height = 3

val RightOfWayScriptingHelper = ScriptingHelper(mapOf(
    String::class to { v: Any? -> "\"\"\"${v.toString()}\"\"\"" },
    Position::class to { v: Any? -> (v as Position).let { "${it.x} to ${it.y}" } },
    Int::class to { v: Any? -> v.toString() },
    Long::class to { v: Any? -> v.toString() },
    IntRange::class to { v: Any? -> (v as IntRange).let { "(${it.first} .. ${it.last})" } },
))

class RightOfWayGenerationEnvironment : ScriptGenerationEnvironment {

    // We want to collapse individual airspace-has-aircraft checks into a single airspace-has-state check,
    // but only if the entire airspace is covered by them.
    val airspaceCheckStates = CheckComposerMap(::AirspaceCheckState)

    class AirspaceCheckState(private val airspaceName: String) : CheckComposer<RightOfWayGenerationEnvironment> {

        private val airspace = mutableMapOf<String, Aircraft>()

        override fun compose(environment: RightOfWayGenerationEnvironment) = if (isFullySpecified) {
            success(listOf(AirspaceScriptGenerationCheckFactory.hasState(airspaceName, airspace)))
        } else {
            failure(IllegalStateException("board has not been fully specified"))
        }

        private val isFullySpecified get() = tokens.size + spaces.size == Width * Height

        fun hasAircraft(player: String, position: Position) = at(position) { tokens[position] = player }
        fun hasSpace(aircraftName: String) = at(aircraftName) { spaces.add(position) }

        private fun at(aircraftName: String, fn: AirspaceCheckState.() -> Unit) =
            if (aircraftName !in airspace.keys) {
                failure(IllegalStateException("Aircraft $aircraftName is not in airspace"))
            } else success(apply(fn))
    }
}

object RightOfWayRunnableScenarioClassGenerator : RunnableScenarioClassGenerator<RightOfWayRunnableScenario>(
    rightOfWayStandardImports.toList(),
    RightOfWayRunnableScenario::class)

// Default imports that should be added to any right-of-way script (generated or parsed).
val rightOfWayStandardImports = arrayOf(
    "com.anaplan.engineering.azuki.rightofway.dsl.*",
    "com.anaplan.engineering.azuki.rightofway.*"
)
