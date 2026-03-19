package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import kotlin.test.*

class CheckComposerTest {

    @Test
    fun mapRegisterReusesSameComposer() {
        assertRegisterReusesSameComposer(keyIncrements = listOf("a", "b", "a", "b", "a"), makeMap())
    }

    @Test
    fun mapRegisterUpdatesMapOnObjectChange() {
        assertRegisterUpdatesRegistryOnObjectChange("a", makeMap())
    }

    @Test
    fun mapTryRegisterRemovesFromMapOnFailure() {
        assertTryRegisterNullifiesOnFailure("a", makeMap())
    }

    @Test
    fun mapPropagatesComposerObjectChanges() {
        assertRegistryPropagatesComposerObjectChanges("a", makeMap())
    }

    @Test
    fun mapPropagatesComposerFailures() {
        assertRegistryPropagatesComposerFailures("a", makeMap())
    }

    @Test
    fun wrapperRegisterReusesSameComposer() {
        // we can consider a wrapper to be a registry with only one key: Unit
        assertRegisterReusesSameComposer(keyIncrements = listOf(Unit, Unit, Unit, Unit, Unit), makeWrapper())
    }

    @Test
    fun wrapperRegisterUpdatesComposerOnObjectChange() {
        assertRegisterUpdatesRegistryOnObjectChange(Unit, makeWrapper())
    }

    @Test
    fun wrapperTryRegisterNullifiesOnFailure() {
        assertTryRegisterNullifiesOnFailure(Unit, makeWrapper())
    }

    @Test
    fun wrapperPropagatesComposerObjectChanges() {
        assertRegistryPropagatesComposerObjectChanges(Unit, makeWrapper())
    }

    @Test
    fun wrapperPropagatesComposerFailures() {
        assertRegistryPropagatesComposerFailures(Unit, makeWrapper())
    }

    abstract class Example(val name: String) : CheckComposer<NoScriptGenerationEnvironment> {

        abstract val counter: Int?
        abstract fun increment(): Example
        abstract fun reverse(): Example
        fun fail(): Result<Example> = Result.failure(IllegalStateException("oops"))

        companion object {

            fun new(name: String): Example = Forwards(name)
        }
    }

    class Forwards(name: String) : Example(name) {

        override var counter = 0
        override fun increment() = apply { counter++ }
        override fun reverse() = Backwards(name)
        override fun compose(environment: NoScriptGenerationEnvironment) =
            Result.success<List<ScriptGenerationCheck<NoScriptGenerationEnvironment>>>(listOf(object :
                ScriptGenerationCheck<NoScriptGenerationEnvironment> {

                override val behavior = unsupportedBehavior
                override fun getCheckScript(environment: NoScriptGenerationEnvironment) = "forwards($counter)"
            }))
    }

    class Backwards(name: String) : Example(name) {

        override var counter = 0
        override fun increment() = apply { counter-- }
        override fun reverse() = Forwards(name)
        override fun compose(environment: NoScriptGenerationEnvironment) =
            Result.success<List<ScriptGenerationCheck<NoScriptGenerationEnvironment>>>(listOf(object :
                ScriptGenerationCheck<NoScriptGenerationEnvironment> {

                override val behavior = unsupportedBehavior
                override fun getCheckScript(environment: NoScriptGenerationEnvironment) = "backwards($counter)"
            }))
    }

    companion object {

        fun makeMap(): CheckComposerMap<NoScriptGenerationEnvironment, String, Example> = CheckComposerMap(Example::new)
        fun makeWrapper(): CheckComposerWrapper<NoScriptGenerationEnvironment, Example> =
            CheckComposerWrapper(Example.new("a"))

        // These tests are identical for maps and wrappers, except for registry type and the key(s) in use:

        private fun <K> assertRegisterReusesSameComposer(
            keyIncrements: List<K>, registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>
        ) {
            keyIncrements.forEach { registry.register(it) { increment() } }

            val keyCounts = keyIncrements.groupBy { it }.mapValues { it.value.size }
            assertEquals(keyCounts.size, registry.composers.size, "should only be as many composers as keys")
            keyCounts.forEach { (k, expected) ->
                assertEquals(expected, registry[k]?.counter, "increments to $k should have been cumulative")
            }
        }

        private fun <K> assertRegisterUpdatesRegistryOnObjectChange(
            key: K, registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>
        ) {
            registry.register(key) { increment() }
            assertIs<Forwards>(registry[key])
            registry.register(key) { reverse() }
            assertIs<Backwards>(registry[key])
        }

        private fun <K> assertTryRegisterNullifiesOnFailure(
            key: K, registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>
        ) {
            registry.register(key) { increment() }
            assertIs<Forwards>(registry[key])
            registry.tryRegister(key) { Result.failure(IllegalStateException("oops")) }
            assertNull(registry[key], "the composer should now appear to have been removed")
        }

        private fun <K> assertRegistryPropagatesComposerObjectChanges(
            key: K, registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>
        ) {
            val composerA = registry.register(key) { increment() }
            assertEquals("forwards(1)", composerA.toScript())
            val composerB = registry.register(key) { reverse() }
            listOf(composerA, composerB).forEach { c -> assertEquals("backwards(0)", c.toScript()) }
            val composerC = registry.register(key) { increment() }
            listOf(composerA, composerB, composerC).forEach { c -> assertEquals("backwards(-1)", c.toScript()) }
            val composerD = registry.tryRegister(key) { fail() }
            listOf(composerA, composerB, composerC, composerD).forEach { c ->
                assertIs<IllegalStateException>(c.compose(NoScriptGenerationEnvironment).exceptionOrNull())
            }
        }

        private fun <K> assertRegistryPropagatesComposerFailures(
            key: K,
            registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>,
        ) {
            val composer = registry.register(key) { increment() }
            assertIs<Forwards>(registry[key])
            registry.tryRegister(key) { Result.failure(IllegalStateException("oops")) }
            assertIs<IllegalStateException>(composer.compose(NoScriptGenerationEnvironment).exceptionOrNull(),
                "should have changed the inner object")
        }
    }
}

private fun CheckComposer<NoScriptGenerationEnvironment>.toScript() =
    compose(NoScriptGenerationEnvironment).getOrThrow()[0].getCheckScript(NoScriptGenerationEnvironment)
