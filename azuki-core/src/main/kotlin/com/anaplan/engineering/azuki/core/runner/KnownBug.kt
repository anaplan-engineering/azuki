package com.anaplan.engineering.azuki.core.runner

/**
 * Used to indicate that that this EAC _should_ pass for the given implementation, but doesn't due to a known issue
 * with a bug (or bugs) raised in JIRA.
 */
// Can't use repeatable due to https://youtrack.jetbrains.com/issue/KT-12794
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class KnownBug(vararg val issues: com.anaplan.engineering.azuki.core.runner.Issue)
