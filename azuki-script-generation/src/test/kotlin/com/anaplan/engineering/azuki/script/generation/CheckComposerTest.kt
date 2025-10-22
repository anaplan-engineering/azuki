package com.anaplan.engineering.azuki.script.generation

import org.junit.Test

import org.junit.Assert.*
import kotlin.test.assertIs
import kotlin.test.expect

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
        assertIs<Success>(map["a"])
        map.register("a") { fail() }
        assertIs<Fail>(map["a"])
    }

    @Test
    fun mapPropagatesComposerObjectChanges() {
        val map = CheckComposerMap(Example::new)
        val env = NoScriptGenerationEnvironment
        val composerA = map.register("a") { increment() }
        assertTrue("first composer should initially succeed", composerA.compose(env).isSuccess)
        val composerB = map.register("a") { fail() }
        assertTrue("second composer should fail", composerB.compose(env).isFailure)
        assertTrue("first composer should now also fail", composerA.compose(env).isFailure)
    }

    @Test
    fun wrapperRegisterReusesSameComposer() {
        val original = Example.new("a")
        val wrapper = CheckComposerWrapper(original)
        wrapper.register { increment() }
        wrapper.register { increment() }
        wrapper.register { increment() }
        wrapper.register { increment() }
        wrapper.register { increment() }
        expect(original, "should be wrapping the same object") { wrapper.inner }
        expect(5, "should have mutated the same object") { wrapper.inner.counter }
    }

    @Test
    fun wrapperPropagatesComposerObjectChanges() {
        val original = Example.new("a")
        val wrapper = CheckComposerWrapper(original)
        expect(original, "should have stored the original object") { wrapper.inner }
        wrapper.register { fail() }
        assertIs<Fail>(wrapper.inner, "should have changed the inner object")
    }

    interface Example : CheckComposer<NoScriptGenerationEnvironment> {

        val counter : Int?
        fun increment(): Example
        fun fail(): Example

        companion object {

            fun new(name: String) : Example = Success(name)
        }
    }

    class Success(val name: String) : Example {

        override var counter = 0
        override fun increment() = apply { counter++ }
        override fun fail() = Fail
        override fun compose(environment: NoScriptGenerationEnvironment) =
            Result.success<List<ScriptGenerationCheck<NoScriptGenerationEnvironment>>>(emptyList())
    }

    object Fail : Example {

        override val counter = null
        override fun increment() = this
        override fun fail() = this
        override fun compose(environment: NoScriptGenerationEnvironment) =
            Result.failure<List<ScriptGenerationCheck<NoScriptGenerationEnvironment>>>(IllegalStateException("failed"))
    }
}
