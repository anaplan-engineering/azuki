package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.FunctionalElement
import com.anaplan.engineering.azuki.core.system.ImplementationVersion
import com.anaplan.engineering.azuki.script.generation.Formatter
import com.anaplan.engineering.azuki.script.generation.ScriptType
import com.anaplan.engineering.azuki.script.generation.runnable.KotlinName.Companion.toKotlinName

/**
 * A runnable scenario renderer.
 */
class RunnableScenarioClassRenderer private constructor() {

    /**
     * Package and class names that are going to be imported at the start of the script.
     * This will be added to during the rendering process, but can also be preloaded during configuration to add
     * in imports.
     */
    var imports = mutableSetOf(
        KotlinName.wildcard("com.anaplan.engineering.azuki.core.runner"),
        KotlinName.wildcard("com.anaplan.engineering.azuki.core.system"),
    )

    /**
     * Map from behavioral constants to their definitions (captured as `KotlinName`s).
     *
     * This is used to prettify BEH annotations.
     *
     * It should be the case that, for each `(key, value)` pair mapped by this function, if `value` is present in
     * the classpath then it evaluates to `key`.
     */
    var getBehaviourKotlinName: (Behavior) -> KotlinName? = { null }

    /**
     * Map from functional element constants to their definitions (captured as `KotlinName`s).
     *
     * This is used to prettify BEH annotations.
     *
     * It should be the case that, for each `(key, value)` pair mapped by this function, if `value` is present in
     * the classpath then it evaluates to `key`.
     */
    var getFunctionalElementKotlinName: (FunctionalElement) -> KotlinName? = { null }

    /**
     * Map from implementation name values to their definitions (captured as `KotlinName`s).
     *
     * This is used to prettify annotations that rely on implementation names.
     *
     * It should be the case that, for each `(key, value)` pair mapped by this function, if `value` is present in
     * the classpath then it evaluates to `key`.
     */
    var getImplementationKotlinName: (String) -> KotlinName? = { null }

    private val builder = StringBuilder()
    private var indentLevel: Int = 0

    private fun doRendering(scenario: RunnableScenarioClassScript) {
        require(builder.isEmpty()) { "shouldn't re-use a renderer" }

        scenario.beh?.let(::beh)
        builder.append("class ${kotlinName(scenario.testName)} : ${kotlinName(scenario.baseName)}() {")
        indentLevel++
        scenario.methods.forEach {
            builder.appendLine().appendLine()
            renderMethod(it)
        }
        indentLevel--
        builder.appendLine().append("}")
    }

    private fun renderMethod(method: RunnableScenarioClassScript.MethodScript) {
        annotations(method.annotations)
        scenarioType(method.type)
        builder.append(method.body.render {
            scriptType = ScriptType.method(method.name)
            indentLevel = this@RunnableScenarioClassRenderer.indentLevel

            // Don't format here, we'll format the JUnit in one go
            formatter = Formatter.None
        })
    }

    private fun annotations(annotations: RunnableScenarioAnnotations) {
        annotations.knownBug?.let(::knownBug)
        annotations.since?.let(::since)
    }

    private fun scenarioType(type: RunnableScenarioMethodType) {
        annotation(type.kotlinName) {
            when (type) {
                is AdapterTestMethodType -> adapterTestMethodType(type)
                is EacMethodType -> eacMethodType(type)
            }
        }
    }

    private fun AnnotationFragment.adapterTestMethodType(type: AdapterTestMethodType) {
        if (type.annotation.expectSkip) {
            member { append("expectSkip = true") }
        }
    }

    private fun AnnotationFragment.eacMethodType(type: EacMethodType) {
        member { string(type.annotation.summary) }
        members(type.annotation.notes) { string(it) }
    }

    private fun beh(beh: BEH) {
        annotation(BEH::class.toKotlinName()) {
            member {
                val behavior = beh.behavior
                getBehaviourKotlinName(behavior)?.let { append(kotlinName(it)) } ?: append(behavior.toString())
            }
            member {
                val fe = beh.functionalElement
                getFunctionalElementKotlinName(fe)?.let { append(kotlinName(it)) } ?: append(fe.toString())
            }
            member { string(beh.summary) }
        }
    }

    private fun knownBug(knownBug: KnownBug) {
        annotation(KnownBug::class.toKotlinName()) {
            knownBug.issues.forEach { member { issue(it) } }
        }
    }

    private fun issue(issue: Issue) {
        nestedAnnotation(Issue::class.toKotlinName()) {
            member {
                val impl = issue.implementation
                getImplementationKotlinName(impl)?.let { append(kotlinName(it)) } ?: string(impl)
            }
            members(issue.jiraIds) { string(it) }
        }
    }

    private fun since(since: Since) {
        annotation(Since::class.toKotlinName()) {
            since.implementationVersion.forEach { member { implementationVersion(it) } }
        }
    }

    private fun implementationVersion(version: ImplementationVersion) {
        nestedAnnotation(ImplementationVersion::class.toKotlinName()) {
            member {
                val impl = version.name
                getImplementationKotlinName(impl)?.let { append(kotlinName(it)) } ?: string(impl)
            }
            member { string(version.version) }
        }
    }

    private fun kotlinName(name: KotlinName) = name.also(::ensureKotlinNameIsImported).identifier

    private fun ensureKotlinNameIsImported(type: KotlinName) {
        if (imports.none { it.satisfiesImport(type) }) imports.add(type)
    }

    private fun annotation(type: KotlinName, body: AnnotationFragment.() -> Unit = {}) {
        indent()
        builder.append("@${kotlinName(type)}")
        AnnotationFragment(builder).apply(body).end()
        builder.appendLine()
    }

    private fun nestedAnnotation(type: KotlinName, body: AnnotationFragment.() -> Unit) {
        builder.append(kotlinName(type))
        AnnotationFragment(builder).apply(body).end()
    }

    private fun indent() = builder.append("    ".repeat(indentLevel))

    companion object {

        /**
         * Renders a runnable scenario class script, optionally with configuration.
         */
        fun RunnableScenarioClassScript.render(config: RunnableScenarioClassRenderer.() -> Unit = {}): String {
            val renderer = RunnableScenarioClassRenderer()
            renderer.config()

            val packageName = testName.packageName
            renderer.doRendering(this)

            return buildString {
                if (packageName.isNotEmpty()) {
                    appendLine("package $packageName").appendLine()
                }

                renderer.imports.sorted().joinTo(this, "\n", postfix = "\n\n") { "import ${it.import}" }

                appendLine(renderer.builder)
            }
        }
    }
}

/**
 * Helper for constructing bits of annotations.
 */
internal class AnnotationFragment(private val parent: Appendable) {

    private var hasMembers = false

    fun member(body: Appendable.() -> Unit) {
        parent.append(memberPrefix)
        parent.body()
        hasMembers = true
    }

    fun <T> members(items: Array<T>, builder: Appendable.(T) -> Unit) {
        if (items.isEmpty()) return
        items.joinTo(parent, prefix = memberPrefix, separator = ", ", transform = {
            buildString { builder(it) }
        })
        hasMembers = true
    }

    fun end() {
        if (hasMembers) parent.append(")")
    }

    private val memberPrefix get() = if (hasMembers) ", " else "("
}

private val specials = Regex("[\\v\"$]+")

internal fun Appendable.string(str: String) {
    if (str.contains(specials)) {
        append("\"\"\"")
        append(str.replace("$", "\${'$'}").replace("\"\"\"", "\${'\"'}\${'\"'}\${'\"'}"))
        append("\"\"\"")
    } else {
        append('"')
        append(str)
        append('"')
    }
}
