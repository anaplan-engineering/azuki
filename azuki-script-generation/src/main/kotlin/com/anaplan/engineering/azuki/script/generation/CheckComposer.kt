package com.anaplan.engineering.azuki.script.generation

fun interface CheckComposer<E : ScriptGenerationEnvironment> {

    /**
     * Resolve the final composed checks to substitute for the original check submitted for composition.
     *
     * This should be called once all checks have been composed into the environment, and in the same relative position
     * in the check order as the original check.  Since each check that contributed to the composition will result in
     * a resolver call, the resolver should make sure that it doesn't duplicate any composed checks.
     *
     * Fails if composition isn't possible, at which point we should return the original check.
     */
    fun compose(environment: E): Result<List<ScriptGenerationCheck<E>>>
}

/**
 * Holds a keyed map of check composers.
 */
class CheckComposerMap<E : ScriptGenerationEnvironment, K, S : CheckComposer<E>>(val constructor: (K) -> S) {

    private val map = mutableMapOf<K, Wrapper<E, S>>()

    /**
     * Registers a check for composition under the check state addressed by the given key.
     * Applies the given transformation to the check state to capture the knowledge added from the check.
     */
    fun register(key: K, effect: S.() -> S): CheckComposer<E> =
        map.getOrPut(key) { Wrapper(constructor(key)) }.transform(effect)

    /**
     * Wraps a composer to ensure that effects that replace it with another object propagate correctly to the generator.
     */
    class Wrapper<E : ScriptGenerationEnvironment, S : CheckComposer<E>>(var inner: S) : CheckComposer<E> {

        fun transform(effect: S.() -> S) = apply { inner = inner.effect() }
        override fun compose(environment: E): Result<List<ScriptGenerationCheck<E>>> = inner.compose(environment)
    }

    operator fun get(key: K): S? = map[key]?.inner

    val composers get() = map.values.map { it.inner }
}
