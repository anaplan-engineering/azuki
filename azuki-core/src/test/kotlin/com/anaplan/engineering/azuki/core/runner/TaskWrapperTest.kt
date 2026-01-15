package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.core.system.SystemFactory
import kotlin.concurrent.thread
import kotlin.test.*

class TaskWrapperTest {

    @Test
    fun taskWrapperCapturesStdout() {
        val wrapper = TaskWrapper(TaskType.Verify, DummyImplementation) {
            repeat(10) { println("hello, world") }
        }

        val result = wrapper.run(DummyScenario)

        assertEquals("hello, world\n".repeat(10), result.log.output)
    }

    @Test
    fun taskWrapperCapturesStderr() {
        val wrapper = TaskWrapper(TaskType.Verify, DummyImplementation) {
            repeat(10) { System.err.println("hello, world") }
        }

        val result = wrapper.run(DummyScenario)

        assertEquals("hello, world\n".repeat(10), result.log.error)
    }

    // TODO: add concurrency tests here

    object DummyScenario : BuildableScenario<ActionFactory> {
        override fun declarations(actionFactory: ActionFactory) = listOf<Action>()
        override fun commands(actionFactory: ActionFactory) = listOf<Action>()
    }

    object DummyImplementation : Implementation<ActionFactory, CheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults> {
        override val name = "dummy"
        override val implementationDefaults = NoSystemDefaults

        override fun createSystemFactory(systemDefaults: NoSystemDefaults): SystemFactory<ActionFactory, CheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults, *> {
            TODO("Not yet implemented")
        }

        override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter
    }
}
