package com.anaplan.engineering.azuki.rightofway.implementation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.io.File
import java.util.ServiceLoader

class AirspaceManager(private val store: File) {

    private val airspaces = mutableMapOf<String, Airspace>()

    operator fun get(name: String) = airspaces[name] ?: throw IllegalArgumentException("Unknown airspace $name")

    fun close(name: String) {
        airspaces.remove(name)
        Log.info("Closed $name, active airspaces = ${airspaces.keys}")
    }

    val activeAirspaces: Collection<String> by airspaces::keys

    fun load(name: String): Airspace {
        val file = airspaceFile(name)
        Log.info("Loading airspace $name from $file")
        return add(name, airspaceCreator.create(objectMapper.readValue<AirspaceState>(file)))
    }

    fun save(name: String) {
        val file = airspaceFile(name)
        Log.info("Saving $name to $file")
        objectMapper.writeValue(file, get(name).state)
    }

    private fun airspaceFile(name: String) = File(store, name)

    fun add(name: String, airspace: Airspace): Airspace {
        if (airspaces.containsKey(name)) {
            throw IllegalArgumentException("Game '$name' already exists")
        }
        airspaces[name] = airspace
        Log.info("Added airpsace: (${airspace::class.simpleName})\n$airspace")
        Log.info("Active airspaces = ${airspaces.keys}")
        return airspace
    }

    private val objectMapper = jacksonObjectMapper()

    val airspaceCreator: AirspaceCreator by lazy {
        val loader = ServiceLoader.load(AirspaceCreator::class.java)
        loader.iterator().next()
    }

    companion object {
        private val Log = LoggerFactory.getLogger(Airspace::class.java)
    }

}

interface AirspaceCreator {
    fun create(state: AirspaceState): Airspace

    // prepopulates data is non-mutable and useful for testing
    fun create(prepopulated: AircraftData = emptyMap()) : Airspace
}
