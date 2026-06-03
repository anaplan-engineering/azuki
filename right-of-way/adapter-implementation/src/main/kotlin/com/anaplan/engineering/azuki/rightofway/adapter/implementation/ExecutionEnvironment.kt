package com.anaplan.engineering.azuki.rightofway.adapter.implementation

import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import com.anaplan.engineering.azuki.rightofway.implementation.AirspaceManager
import kotlin.text.get

class ExecutionEnvironment(val airspaceManager: AirspaceManager) {

    val playOrders = mutableMapOf<String, List<String>>()

    fun <T> withAirspace(name: String, op: Airspace.() -> T) = airspaceManager[name].op()
}
