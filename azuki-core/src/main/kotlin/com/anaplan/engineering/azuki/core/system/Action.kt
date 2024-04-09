package com.anaplan.engineering.azuki.core.system

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

interface ActionFactory

interface Action : ReifiedBehavior

interface ParallelActionFactory<T : Action> {
    fun createParallelAction(actions: List<List<Action>>): ParallelAction<T>
}

abstract class ParallelAction<T : Action>(
    val actions: List<List<T>>
) : Action {
    override val behavior: Behavior = parallel

    protected fun <R> runActionsConcurrently(singleActionRunner: (T) -> R) =
        runBlocking {
            actions.map { crActions ->
                async {
                    crActions.map(singleActionRunner)
                }
            }.map {
                it.await()
            }
        }

    protected fun <R> runActionsSequentially(singleActionRunner: (T) -> R) =
        actions.map { crActions ->
            crActions.map(singleActionRunner)
        }
}

object UnsupportedAction : Action {
    override val behavior: Behavior = unsupportedBehavior
}

class LateDetectUnsupportedActionException(msg: String? = null) : Exception(msg)
