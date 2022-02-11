package com.anaplan.engineering.azuki.core.system


interface Check : ReifiedBehavior

interface CheckFactory

object UnsupportedCheck : Check {
    override val behavior = unsupportedBehavior
}

class LateDetectUnsupportedCheckException(msg: String? = null) : Exception(msg)
