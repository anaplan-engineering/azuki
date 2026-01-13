package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.FunctionalElement
import com.anaplan.engineering.azuki.core.system.ImplementationVersion
import com.anaplan.engineering.azuki.script.generation.Formatter
import com.anaplan.engineering.azuki.script.generation.RenderContext
import com.anaplan.engineering.azuki.script.generation.ScriptType
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier

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
        Importable.wildcard("com.anaplan.engineering.azuki.core.runner"),
        Importable.wildcard("com.anaplan.engineering.azuki.core.system"),
    )

    /**
     * Map from behavioral constants to their definitions (captured as qualified identifiers).
     *
     * This is used to prettify BEH annotations.
     *
     * It should be the case that, for each `(key, value)` pair mapped by this function, if `value` is present in
     * the classpath then it evaluates to `key`.
     */
    var getBehaviourKotlinName: (Behavior) -> QualifiedIdentifier? = { null }

    /**
     * Map from functional element constants to their definitions (captured as qualified identifiers).
     *
     * This is used to prettify BEH annotations.
     *
     * It should be the case that, for each `(key, value)` pair mapped by this function, if `value` is present in
     * the classpath then it evaluates to `key`.
     */
    var getFunctionalElementKotlinName: (FunctionalElement) -> QualifiedIdentifier? = { null }

    /**
     * Map from implementation name values to their definitions (captured as qualified identifiers).
     *
     * This is used to prettify annotations that rely on implementation names.
     *
     * It should be the case that, for each `(key, value)` pair mapped by this function, if `value` is present in
     * the classpath then it evaluates to `key`.
     */
    var getImplementationKotlinName: (String) -> QualifiedIdentifier? = { null }

    /**
     * The starting render context for the renderer.
     */
    var startingRenderContext = RenderContext()

    private val builder = StringBuilder()

    private fun doRendering(scenario: RunnableScenarioClassScript) {
        require(builder.isEmpty()) { "shouldn't re-use a renderer" }

        scenario.beh?.let { beh(startingRenderContext, it) }
        builder.append("class ${identifier(scenario.testName)} : ${identifier(scenario.baseName)}() {")
        scenario.methods.forEach {
            builder.appendLine().appendLine()
            renderMethod(startingRenderContext.nextIndentLevel, it)
        }
        builder.appendLine().append("}")
    }

    private fun renderMethod(ctx: RenderContext, method: RunnableScenarioClassScript.MethodScript) {
        annotations(ctx, method.annotations)
        scenarioType(ctx, method.type)
        builder.append(method.body.render {
            scriptType = ScriptType.method(method.name)
            startingRenderContext = ctx

            // Don't format here, we'll format the JUnit in one go
            formatter = Formatter.None
        })
    }

    private fun annotations(ctx: RenderContext, annotations: RunnableScenarioAnnotations) {
        annotations.knownBug?.let { knownBug(ctx, it) }
        annotations.since?.let {since(ctx, it) }
    }

    private fun scenarioType(ctx: RenderContext, type: RunnableScenarioMethodType) {
        annotation(ctx, type.kotlinName) {
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

    private fun beh(ctx: RenderContext, beh: BEH) {
        annotation(ctx, BEH::class.toQualifiedIdentifier()) {
            member {
                val behavior = beh.behavior
                getBehaviourKotlinName(behavior)?.let { append(identifier(it)) } ?: append(behavior.toString())
            }
            member {
                val fe = beh.functionalElement
                getFunctionalElementKotlinName(fe)?.let { append(identifier(it)) } ?: append(fe.toString())
            }
            member { string(beh.summary) }
        }
    }

    private fun knownBug(ctx: RenderContext, knownBug: KnownBug) {
        annotation(ctx, KnownBug::class.toQualifiedIdentifier()) {
            knownBug.issues.forEach { member { issue(it) } }
        }
    }

    private fun issue(issue: Issue) {
        nestedAnnotation(Issue::class.toQualifiedIdentifier()) {
            member {
                val impl = issue.implementation
                getImplementationKotlinName(impl)?.let { append(identifier(it)) } ?: string(impl)
            }
            members(issue.jiraIds) { string(it) }
        }
    }

    private fun since(ctx: RenderContext, since: Since) {
        annotation(ctx, Since::class.toQualifiedIdentifier()) {
            since.implementationVersion.forEach { member { implementationVersion(it) } }
        }
    }

    private fun implementationVersion(version: ImplementationVersion) {
        nestedAnnotation(ImplementationVersion::class.toQualifiedIdentifier()) {
            member {
                val impl = version.name
                getImplementationKotlinName(impl)?.let { append(identifier(it)) } ?: string(impl)
            }
            member { string(version.version) }
        }
    }

    private fun identifier(name: QualifiedIdentifier) = name.also(::ensureIdentifierIsImported).identifier

    private fun ensureIdentifierIsImported(type: QualifiedIdentifier) {
        if (imports.none { it canBeUsedToImport type }) imports.add(type)
    }

    private fun annotation(ctx: RenderContext, type: QualifiedIdentifier, body: AnnotationFragment.() -> Unit = {}) {
        builder.append("${ctx.indent}@${identifier(type)}")
        AnnotationFragment(builder).apply(body).end()
        builder.appendLine()
    }

    private fun nestedAnnotation(type: QualifiedIdentifier, body: AnnotationFragment.() -> Unit) {
        builder.append(identifier(type))
        AnnotationFragment(builder).apply(body).end()
    }

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
