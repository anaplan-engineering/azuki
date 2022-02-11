package com.anaplan.engineering.azuki.core.runner

/**
 * Used to indicate that there is planned work that will provide an unimplemented feature that will enable this EAC to
 * pass.
 */
// Can't use repeatable due to https://youtrack.jetbrains.com/issue/KT-12794
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ToBeDone(vararg val issues: com.anaplan.engineering.azuki.core.runner.Issue)

@Retention(AnnotationRetention.RUNTIME)
annotation class Issue(val implementation: String, vararg val jiraIds: String)

