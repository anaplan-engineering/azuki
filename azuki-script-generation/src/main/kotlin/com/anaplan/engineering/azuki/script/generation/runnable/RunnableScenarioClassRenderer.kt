package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.runner.ToBeDone
import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.FunctionalElement
import com.anaplan.engineering.azuki.core.system.ImplementationVersion
import com.anaplan.engineering.azuki.script.generation.Formatter
import com.anaplan.engineering.azuki.script.generation.RenderContext
import com.anaplan.engineering.azuki.script.generation.ScriptType
import com.anaplan.engineering.azuki.script.generation.literal
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
     * Renderers for scenario method annotations.
     *
     * This associative list should be ordered in the top-down order the annotations should appear in the script, and
     * each renderer should return true if it successfully handled the annotation.
     *
     * See `annotationRenderer` for a helper to create entries of the right type.  The default value is OK whenever the
     * Azuki use case doesn't have its own custom annotations or preferences on their order.
     */
    var methodAnnotationRenderers =
        mutableListOf<Function3<RunnableScenarioClassRenderer, RenderContext, Annotation, Boolean>>(annotationRenderer(
            RunnableScenarioClassRenderer::knownBug),
            annotationRenderer(RunnableScenarioClassRenderer::toBeDone),
            annotationRenderer(RunnableScenarioClassRenderer::since))

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
     * The top-level render context for the renderer.
     */
    var topLevelRenderContext = RenderContext()

    private val builder = StringBuilder()

    private fun doRendering(scenario: RunnableScenarioClassScript) {
        require(builder.isEmpty()) { "shouldn't re-use a renderer" }

        scenario.beh?.let { beh(topLevelRenderContext, it) }
        builder.append("class ${identifier(scenario.testName)} : ${identifier(scenario.baseName)}() {")
        scenario.methods.forEach {
            builder.appendLine().appendLine()
            renderMethod(topLevelRenderContext.nextIndentLevel, it)
        }
        builder.appendLine().append("}")
    }

    private fun renderMethod(ctx: RenderContext, method: RunnableScenarioClassScript.MethodScript) {
        annotations(ctx, method.annotations)
        scenarioType(ctx, method.type)
        builder.append(method.body.render {
            scriptType = ScriptType.method(method.name)
            topLevelRenderContext = ctx

            // Don't format here, we'll format the JUnit in one go
            formatter = Formatter.None
        })
    }

    private fun annotations(ctx: RenderContext, annotations: Set<Annotation>) {
        // Quadratic, but we shouldn't have that many annotations.
        methodAnnotationRenderers.forEach { renderer ->
            for (ann in annotations) {
                val success = renderer(this, ctx, ann)
                if (success) {
                    break
                }
            }
        }
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
            +"expectSkip = true"
        }
    }

    private fun AnnotationFragment.eacMethodType(type: EacMethodType) {
        +type.annotation.summary.literal
        type.annotation.notes.forEach { +it.literal }
    }

    private fun beh(ctx: RenderContext, beh: BEH) {
        annotation(ctx, BEH::class.toQualifiedIdentifier()) {
            val behavior = beh.behavior
            +(getBehaviourKotlinName(behavior)?.let(::identifier) ?: behavior.toString())

            val fe = beh.functionalElement
            +(getFunctionalElementKotlinName(fe)?.let(::identifier) ?: fe.toString())

            +beh.summary.literal
        }
    }

    internal fun identifier(name: QualifiedIdentifier) = name.also(::ensureIdentifierIsImported).identifier

    private fun ensureIdentifierIsImported(type: QualifiedIdentifier) {
        if (imports.none { it canBeUsedToImport type }) {
            imports.add(type)
        }
    }

    /**
     * Renders a 'KnownBug' annotation.
     */
    fun knownBug(ctx: RenderContext, knownBug: KnownBug) {
        annotation(ctx, KnownBug::class.toQualifiedIdentifier()) {
            knownBug.issues.forEach { +issue(it) }
        }
    }

    /**
     * Renders a 'ToBeDone' annotation.
     */
    fun toBeDone(ctx: RenderContext, toBeDone: ToBeDone) {
        annotation(ctx, KnownBug::class.toQualifiedIdentifier()) {
            toBeDone.issues.forEach { +issue(it) }
        }
    }

    private fun issue(issue: Issue) = nestedAnnotation(Issue::class.toQualifiedIdentifier()) {
        val implName = getImplementationKotlinName(issue.implementation)
        +(implName?.let(::identifier) ?: issue.implementation.literal)

        issue.jiraIds.forEach { +it.literal }
    }

    /**
     * Renders a 'Since' annotation.
     */
    fun since(ctx: RenderContext, ann: Since) {
        annotation(ctx, Since::class.toQualifiedIdentifier()) {
            ann.implementationVersion.forEach { +implementationVersion(it) }
        }
    }

    private fun implementationVersion(version: ImplementationVersion) =
        nestedAnnotation(ImplementationVersion::class.toQualifiedIdentifier()) {
            val implName = getImplementationKotlinName(version.name)
            +(implName?.let(::identifier) ?: version.name.literal)

            +version.version.literal
        }

    internal fun annotation(ctx: RenderContext, type: QualifiedIdentifier, body: AnnotationFragment.() -> Unit = {}) {
        builder.append("${ctx.indent}@${identifier(type)}")
        AnnotationFragment(builder).apply(body).end()
        builder.appendLine()
    }

    internal fun nestedAnnotation(type: QualifiedIdentifier, body: AnnotationFragment.() -> Unit) = buildString {
        append(identifier(type))
        AnnotationFragment(this).apply(body).end()
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

        /**
         * Creates a new annotation renderer from a reified rendering function.
         */
        inline fun <reified T : Annotation> annotationRenderer(crossinline f: RunnableScenarioClassRenderer.(RenderContext, T) -> Unit) =
            fun(
                parent: RunnableScenarioClassRenderer, context: RenderContext, annotation: Annotation
            ) = if (annotation is T) {
                parent.f(context, annotation)
                true
            } else {
                false
            }
    }
}

/**
 * Helper for constructing bits of annotations.
 */
internal class AnnotationFragment(private val parent: Appendable) {

    private var hasMembers = false

    /**
     * Adds a string into an annotation fragment.
     */
    operator fun String.unaryPlus() {
        parent.append(memberPrefix)
        parent.append(this)
        hasMembers = true
    }

    fun end() {
        if (hasMembers) {
            parent.append(")")
        }
    }

    private val memberPrefix get() = if (hasMembers) ", " else "("
}
