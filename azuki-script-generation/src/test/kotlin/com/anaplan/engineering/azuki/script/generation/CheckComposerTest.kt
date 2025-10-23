package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import kotlin.test.*

class CheckComposerTest {

    @Test
    fun mapRegisterReusesSameComposer() {
        val map = CheckComposerMap(Example::new)
        map.register("a") { increment() }
        map.register("b") { increment() }
        map.register("a") { increment() }
        map.register("b") { increment() }
        map.register("a") { increment() }

        expect(2, "should only be two composers") { map.composers.size }
        expect(3, "increments to a should have been cumulative") { map["a"]?.counter }
        expect(2, "increments to b should have been cumulative") { map["b"]?.counter }
    }

    @Test
    fun mapRegisterUpdatesMapOnObjectChange() {
        val map = CheckComposerMap(Example::new)
        map.register("a") { increment() }
        assertIs<Forwards>(map["a"])
        map.register("a") { reverse() }
        assertIs<Backwards>(map["a"])
    }

    @Test
    fun mapRegisterRemovesFromMapOnFailure() {
        val map = CheckComposerMap(Example::new)
        map.register("a") { increment() }
        assertIs<Forwards>(map["a"])
        map.tryRegister("a") { Result.failure(IllegalStateException("oops")) }
        assertNull(map["a"], "a should now appear to have been removed")
    }

    @Test
    fun mapPropagatesComposerObjectChanges() {
        val map = CheckComposerMap(Example::new)
        val composerA = map.register("a") { increment() }
        expect("forwards(1)") { composerA.toScript() }
        val composerB = map.register("a") { reverse() }
        listOf(composerA, composerB).forEach { c -> expect("backwards(0)") { c.toScript() } }
        val composerC = map.register("a") { increment() }
        listOf(composerA, composerB, composerC).forEach { c -> expect("backwards(-1)") { c.toScript() } }
        val composerD = map.tryRegister("a") { fail() }
        listOf(composerA, composerB, composerC, composerD).forEach { c ->
            assertIs<IllegalStateException>(c.compose(NoScriptGenerationEnvironment).exceptionOrNull())
        }
    }

    @Test
    fun wrapperRegisterReusesSameComposer() {
        val original = Example.new("a")
        val wrapper = CheckComposerWrapper(original)
        expect("forwards(0)", "should have stored the original object") { wrapper.toScript() }
        wrapper.register { increment() }
        wrapper.register { increment() }
        wrapper.register { increment() }
        wrapper.register { increment() }
        wrapper.register { increment() }
        expect("forwards(5)", "should have mutated the same object") { wrapper.toScript() }
    }

    @Test
    fun wrapperPropagatesComposerObjectChanges() {
        val original = Example.new("a")
        val wrapper = CheckComposerWrapper(original)
        expect("forwards(0)", "should have stored the original object") { wrapper.toScript() }
        wrapper.register { reverse() }
        expect("backwards(0)", "should have changed the inner object") { wrapper.toScript() }
    }

    @Test
    fun wrapperPropagatesComposerFailures() {
        val original = Example.new("a")
        val wrapper = CheckComposerWrapper(original)
        expect("forwards(0)", "should have stored the original object") { wrapper.toScript() }
        wrapper.tryRegister { Result.failure(IllegalStateException("oops")) }
        assertIs<IllegalStateException>(wrapper.compose(NoScriptGenerationEnvironment).exceptionOrNull(),
            "should have changed the inner object")
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
}

private fun CheckComposer<NoScriptGenerationEnvironment>.toScript() =
    compose(NoScriptGenerationEnvironment).getOrThrow()[0].getCheckScript(NoScriptGenerationEnvironment)
