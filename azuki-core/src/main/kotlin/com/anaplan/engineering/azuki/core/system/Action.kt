package com.anaplan.engineering.azuki.core.system

interface ActionFactory

interface Action : ReifiedBehavior

object UnsupportedAction : Action {
    override val behavior: Behavior = unsupportedBehavior
}

class LateDetectUnsupportedActionException(msg: String? = null) : Exception(msg)
