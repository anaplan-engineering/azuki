package com.anaplan.engineering.azuki.script.generation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

    companion object {

        internal val Log: Logger = LoggerFactory.getLogger(CheckComposer::class.java)
    }
}

/**
 * Handles registering check composers against keys, allowing discovery and composition of related checks.
 *
 * A typical use case is for composable checks that constrain a particular domain object to <code>register</code>
 * themselves in a registry corresponding to a group of checks that can be composed into each other.  Each object is
 * registered under a key that uniquely identifies that object.  Each registration then adds that check's information to
 * a composer for that key, ensuring that each domain object has one and only one distinct composer.  Said composer is
 * then returned by <code>register</code>, and eventually used to look up the checks that resulted from the composition
 * effort.
 */
interface CheckComposerRegistry<E : ScriptGenerationEnvironment, K, S : CheckComposer<E>> {

    /**
     * Registers a check for composition inside this map, under the given key and with the given effect.
     *
     * If an existing composer exists under the same key, the effect is applied cumulatively to it.
     */
    fun register(key: K, effect: S.() -> S): CheckComposer<E> = tryRegister(key) {
        Result.success(effect())
    }

    /**
     * As with <code>register()</code>, but can fail, permanently halting composition for this key.
     */
    fun tryRegister(key: K, effect: S.() -> Result<S>): CheckComposer<E>

    /**
     * Gets the current composer for a given key, if one exists.
     *
     * It is not safe to use the output of this function after calling <code>register</code> or <code>tryRegister</code>
     * on this key, as they may cause the composer under that key to change its object identity.
     */
    operator fun get(key: K): S?

    /**
     * Gets all currently-active composers registered in this registry.
     *
     * It is not safe to use the value of this property after calling <code>register</code> or <code>tryRegister</code>,
     * as they may cause composers to fail or
     */
    val composers: Collection<S>
}

/**
 * Implements a check composer registry with a keyed map.
 *
 * This is usable directly or through delegation from another <code>CheckComposerRegistry</code>.
 */
class CheckComposerMap<E : ScriptGenerationEnvironment, K, S : CheckComposer<E>>(val constructor: (K) -> S) :
    CheckComposerRegistry<E, K, S> {

    /* We use CheckComposerWrapper here because, when register/tryRegister are applied to an existing key, they can
     * replace the underlying S object inside the map.  This would ordinarily leave the CheckComposer<E> that was
     * returned to previous calls dangling and pointing to the old S object.  Instead, we always store the same wrapper,
     * return that wrapper as the CheckComposer<E>, and cascade all registration attempts into the wrapper.
     */
    private val map = mutableMapOf<K, CheckComposerWrapper<E, S>>()

    override fun register(key: K, effect: S.() -> S): CheckComposer<E> = getOrInit(key).register(Unit, effect)
    override fun tryRegister(key: K, effect: S.() -> Result<S>): CheckComposer<E> =
        getOrInit(key).tryRegister(Unit, effect)

    override operator fun get(key: K): S? = map[key]?.get(Unit)
    override val composers get() = map.values.flatMap { it.composers }

    private fun getOrInit(key: K) = map.getOrPut(key) { CheckComposerWrapper(constructor(key)) }
}

/**
 * Wraps a composer to ensure that effects that replace it with another object propagate correctly to the generator.
 *
 * This behaves as a composer registry, but with one fixed key and whose registration functions return the wrapper as
 * a composer in its own right.
 */
class CheckComposerWrapper<E : ScriptGenerationEnvironment, S : CheckComposer<E>>(initial: S) : CheckComposer<E>,
    CheckComposerRegistry<E, Unit, S> {

    private var inner = Result.success(initial)

    override fun register(key: Unit, effect: S.() -> S) = apply { inner = inner.map(effect) }
    override fun tryRegister(key: Unit, effect: S.() -> Result<S>) = apply { inner = bind(effect) }
    override operator fun get(key: Unit): S? = inner.getOrNull()
    override val composers get() = listOfNotNull(inner.getOrNull())

    override fun compose(environment: E): Result<List<ScriptGenerationCheck<E>>> = bind { compose(environment) }

    private fun <T> bind(fn: S.() -> Result<T>) = inner.fold(onSuccess = fn, onFailure = { Result.failure(it) })
}

internal fun <E : ScriptGenerationEnvironment> composeChecks(
    environment: E, checksWithComposers: List<Pair<ScriptGenerationCheck<E>, CheckComposer<E>?>>
): List<ScriptGenerationCheck<E>> {
    val succeeded = mutableSetOf<CheckComposer<E>>()
    val failed = mutableSetOf<CheckComposer<E>>()
    val composedChecks = checksWithComposers.flatMap { (check, composer) ->
        when (composer) {
            // non-composable checks pass through unaltered
            // (also, avoid re-evaluating failed compositions as we assume they'll fail again)
            null, in failed -> listOf(check)
            // only allow a successful composers to be composed once, to avoid duplicates
            in succeeded -> emptyList()
            // otherwise, we're seeing a composable check for the first time
            else -> composer.compose(environment).fold(onSuccess = {
                succeeded.add(composer)
                it
            }, onFailure = {
                failed.add(composer)
                CheckComposer.Log.info("check {} failed to compose: {} ({})", check, it::class.simpleName, it.message)
                listOf(check)
            })
        }
    }
    return composedChecks.distinct()
}
