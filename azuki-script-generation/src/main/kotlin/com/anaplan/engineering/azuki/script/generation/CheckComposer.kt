package com.anaplan.engineering.azuki.script.generation

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.Result.Companion.success

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
 * Helper for creating state machines that produce composed checks.
 *
 * This class handles making sure the composed check is only taken once, and handles state transitions appropriately.
 */
class StateBasedCheckComposer<E: ScriptGenerationEnvironment, S: CheckComposer<E>>(initialState: S) : CheckComposer<E> {

    private var _state: S = initialState
    private var taken: Boolean = false
    val state get() = _state

    /**
     * Registers a check, in the form of a transition on the composer's state.
     */
    fun register(effect: S.() -> S): CheckComposer<E> = apply {
        require(!taken) { "tried to compose a check but we've already taken the composed checks for this check state" }
        _state = _state.effect()
    }

    override fun compose(environment: E) =
        if (taken) success(emptyList()) else {
            _state.compose(environment).onSuccess {
                taken = true
            }.onFailure {
                Log.info("couldn't compose: {} ({})", it::class.simpleName, it.message)
                // don't take the state, otherwise other checks will fail to decompose
            }
        }

    companion object {

        private val Log: Logger = LoggerFactory.getLogger(StateBasedCheckComposer::class.java)
    }
}

/**
 * Holds a keyed map of check-composing state machines.
 */
class CheckComposerMap<E: ScriptGenerationEnvironment, K, S : CheckComposer<E>>(val constructor: (K) -> S) {

    private val map = mutableMapOf<K, StateBasedCheckComposer<E, S>>()

    /**
     * Registers a check for composition under the check state addressed by the given key.
     * Applies the given transformation to the check state to capture the knowledge added from the check.
     */
    fun register(key: K, effect: S.() -> S): CheckComposer<E> = map.getOrPut(key) {
        StateBasedCheckComposer(constructor(key))
    }.register(effect)

    fun stateAt(key: K): S? = map[key]?.state

    val states get() = map.values.map { it.state }
}
