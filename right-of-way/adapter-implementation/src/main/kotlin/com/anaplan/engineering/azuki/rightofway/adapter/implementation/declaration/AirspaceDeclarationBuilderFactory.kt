package com.anaplan.engineering.azuki.rightofway.adapter.implementation.declaration

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class AirspaceDeclarationBuilderFactory : SampleDeclarationBuilderFactory<AirspaceDeclaration> {

    override val declarationClass = AirspaceDeclaration::class.java
    override fun create(declaration: AirspaceDeclaration): SampleDeclarationBuilder<AirspaceDeclaration> =
        AirspaceDeclarationBuilder(declaration)

    private class AirspaceDeclarationBuilder(declaration: AirspaceDeclaration) :
        SampleDeclarationBuilder<AirspaceDeclaration>(declaration) {

        // Maps adapter-api type (Aircrafts = Map<String, Aircraft(Position, Velocity)>) into
        // implementation type (Aircrafts = Map<String, Pair<Pair<Double, Double>, Pair<Double, Double>>)
        //
        // Here the mapping is somewhat artificial (e.g., data class to pair of corresponding types)
        // In practice, adapter-api is an abstraction representation of implementation detailed representation.
        override fun build(env: ExecutionEnvironment) {
            val prepopulated = declaration.aircrafts.map { (name, aircraft) ->
                name to aircraft.toPair()
            }.toMap()
            env.airspaceManager.add(declaration.name,
                env.airspaceManager.airspaceCreator.create(
                    declaration.delta_c, declaration.delta_o,
                    declaration.Theta_h, declaration.opened, prepopulated))
        }
    }
}
