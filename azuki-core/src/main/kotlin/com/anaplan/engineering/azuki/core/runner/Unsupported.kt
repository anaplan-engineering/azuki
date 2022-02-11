package com.anaplan.engineering.azuki.core.runner

/**
 * Used to indicate that there is no intention to support the behavior of this EAC in the given implementation. This
 * annotation should be used rarely and briefly until the implementation's adapter can indicate that the behavior is
 * unsupported (which is necessary for verification).
 */
// Can't use repeatable due to https://youtrack.jetbrains.com/issue/KT-12794
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Unsupported(vararg val implementation: String)
