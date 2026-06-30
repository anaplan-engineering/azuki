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
    fun mapTryRegisterNullifiesOnFailure() {
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

        // These tests are identical for maps and wrappers, except for registry type and the key(s) in use.
        // Note that we could change most tests to take `registry.onKey(key)` directly, but then we'd lose the
        // implicit exercising of assertions about `registry.onKey(key)`'s correctness.

        private fun <K> assertRegisterReusesSameComposer(
            keyIncrements: List<K>, registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>
        ) {
            keyIncrements.forEach { registry.onKey(it).register { increment() } }

            val keyCounts = keyIncrements.groupBy { it }.mapValues { it.value.size }
            assertEquals(keyCounts.size, registry.composers.size, "should only be as many composers as keys")
            keyCounts.forEach { (k, expected) ->
                val actual = registry.onKey(k).composer?.counter
                assertEquals(expected, actual, "increments to $k should have been cumulative")
            }
        }

        private fun <K> assertRegisterUpdatesRegistryOnObjectChange(
            key: K, registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>
        ) {
            registry.onKey(key).register { increment() }
            assertIs<Forwards>(registry.onKey(key).composer)
            registry.onKey(key).register { reverse() }
            assertIs<Backwards>(registry.onKey(key).composer)
        }

        private fun <K> assertTryRegisterNullifiesOnFailure(
            key: K, registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>
        ) {
            registry.onKey(key).register { increment() }
            assertIs<Forwards>(registry.onKey(key).composer)
            registry.onKey(key).tryRegister { Result.failure(IllegalStateException("oops")) }
            assertNull(registry.onKey(key).composer, "the composer should now appear to have been removed")
        }

        private fun <K> assertRegistryPropagatesComposerObjectChanges(
            key: K, registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>
        ) {
            val composerA = registry.onKey(key).register { increment() }
            assertEquals("forwards(1)", composerA.toScript())
            val composerB = registry.onKey(key).register { reverse() }
            listOf(composerA, composerB).forEach { c -> assertEquals("backwards(0)", c.toScript()) }
            val composerC = registry.onKey(key).register { increment() }
            listOf(composerA, composerB, composerC).forEach { c -> assertEquals("backwards(-1)", c.toScript()) }
            val composerD = registry.onKey(key).tryRegister { fail() }
            listOf(composerA, composerB, composerC, composerD).forEach { c ->
                assertIs<IllegalStateException>(c.compose(NoScriptGenerationEnvironment).exceptionOrNull())
            }
        }

        private fun <K> assertRegistryPropagatesComposerFailures(
            key: K,
            registry: CheckComposerRegistry<NoScriptGenerationEnvironment, K, Example>,
        ) {
            val composer = registry.onKey(key).register { increment() }
            assertIs<Forwards>(registry.onKey(key).composer)
            registry.onKey(key).tryRegister { Result.failure(IllegalStateException("oops")) }
            assertIs<IllegalStateException>(composer.compose(NoScriptGenerationEnvironment).exceptionOrNull(),
                "should have changed the inner object")
        }
    }
}

private fun CheckComposer<NoScriptGenerationEnvironment>.toScript() =
    compose(NoScriptGenerationEnvironment).getOrThrow()[0].getCheckScript(NoScriptGenerationEnvironment)
