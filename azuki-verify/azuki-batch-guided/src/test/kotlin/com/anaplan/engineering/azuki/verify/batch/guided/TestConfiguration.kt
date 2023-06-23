package com.anaplan.engineering.azuki.verify.batch.guided

import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.verify.batch.api.ConfigFileProperty
import com.anaplan.engineering.azuki.verify.batch.api.ScenarioResultContext
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class TestConfiguration {

    @Test
    fun loadConfiguration() {
        val resource = javaClass.getResource("/configuration.json")!!
        val configFile = File(resource.toURI()).absolutePath
        System.setProperty(ConfigFileProperty, configFile)
        val batch = GuidedScenarioBatch<BuildableScenario<*>, TestRunContext>()
        val config = batch.runConfiguration
        assertEquals(listOf(File("scenario1.scn"), File("scenario2.scn")), config.scenarios.files)
        assertEquals(3, config.reps)
    }

    private object TestRunContext : ScenarioResultContext {
        override val reportFields = emptyMap<String, String>()
    }

}
