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

    private val map = mutableMapOf<K, CheckComposerWrapper<E, S>>()

    /**
     * Registers a check for composition inside this map, under the given key and with the given effect.
     * If an existing composer exists under the same key, the effect is applied cumulatively to it.
     */
    fun register(key: K, effect: S.() -> S): CheckComposer<E> = getOrInit(key).register(effect)

    /**
     * As with register(), but can fail, permanently halting composition for this key
     */
    fun tryRegister(key: K, effect: S.() -> Result<S>): CheckComposer<E> = getOrInit(key).tryRegister(effect)

    private fun getOrInit(key: K) = map.getOrPut(key) { CheckComposerWrapper(constructor(key)) }

    /**
     * Gets the composer for a key, provided that it hasn't been removed through failure.
     */
    operator fun get(key: K): S? = map[key]?.composer

    val composers get() = map.values.mapNotNull { it.composer }
}

/**
 * Wraps a composer to ensure that effects that replace it with another object propagate correctly to the generator.
 */
class CheckComposerWrapper<E : ScriptGenerationEnvironment, S : CheckComposer<E>>(initial: S) : CheckComposer<E> {

    private var _inner = Result.success(initial)
    val composer: S? get() = _inner.getOrNull()

    /**
     * Registers a check on the inner composer by applying an effect to it.
     * If the effect returns a new object, this wrapper updates to point to it.
     */
    fun register(effect: S.() -> S) = apply { _inner = _inner.map(effect) }

    /**
     * As with register(), but can fail, permanently halting composition.
     */
    fun tryRegister(effect: S.() -> Result<S>) = apply { _inner = bind(effect) }

    override fun compose(environment: E): Result<List<ScriptGenerationCheck<E>>> = bind { compose(environment) }

    private fun <T> bind(fn: S.() -> Result<T>) = _inner.fold(onSuccess = fn, onFailure = { Result.failure(it) })
}
