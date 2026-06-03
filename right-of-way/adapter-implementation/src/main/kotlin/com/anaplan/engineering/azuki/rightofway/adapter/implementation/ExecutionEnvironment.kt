package com.anaplan.engineering.azuki.rightofway.adapter.implementation

import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import com.anaplan.engineering.azuki.rightofway.implementation.AirspaceManager
import kotlin.text.get

class ExecutionEnvironment(val airspaceManager: AirspaceManager) {

    // because of the get method, this always returns non-null
    fun <T> withAirspace(name: String, op: Airspace.() -> T) = airspaceManager[name].op()
}
