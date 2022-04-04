package com.anaplan.engineering.azuki.core.system


interface Check : ReifiedBehavior

interface CheckFactory {
    // a default check that asserts that the system built is valid
    fun systemValid(): Check = UnsupportedCheck
}

object UnsupportedCheck : Check {
    override val behavior = unsupportedBehavior
}

class LateDetectUnsupportedCheckException(msg: String? = null) : Exception(msg)
