package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.script.generation.Formatter
import com.anaplan.engineering.azuki.script.generation.RenderContext
import com.anaplan.engineering.azuki.script.generation.ScriptType
import com.anaplan.engineering.azuki.script.generation.literal
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier

/**
 * A runnable scenario renderer.
 */
class RunnableScenarioClassRenderer private constructor(
    /**
     * Package and class names that are going to be imported at the start of the script.
     * This will be added to during the rendering process, but can also be preloaded during configuration to add
     * in imports.
     */
    val importSet: ImportSet = ImportSet(
        Importable.wildcard("com.anaplan.engineering.azuki.core.runner"),
        Importable.wildcard("com.anaplan.engineering.azuki.core.system"),
    )
) : IdentifierTracker by importSet {

    /**
     * Renderers for scenario method annotations.
     *
     * This associative list should be ordered in the top-down order the annotations should appear in the script, and
     * each renderer should return true if it successfully handled the annotation.
     *
     * See `annotationRenderer` for a helper to create entries of the right type.  The default value is OK whenever the
     * Azuki use case doesn't have its own custom annotations or preferences on their order.
     */
    val methodAnnotationRenderers = mutableListOf(*RunnableScenarioAnnotationRenderer.standardRenderers)

    /**
     * Holds mapping functions from behavioral, functional-element, and implementation constants to their definitions.
     */
    val identifierContext = IdentifierContext(this)

    /**
     * The top-level render context for the renderer.
     */
    var topLevelRenderContext = RenderContext()

    private val builder = StringBuilder()

    private fun doRendering(scenario: RunnableScenarioClassScript) {
        require(builder.isEmpty()) { "shouldn't re-use a renderer" }

        scenario.beh?.let { beh(topLevelRenderContext, it) }
        builder.append("class ${importSet.identifier(scenario.testName)}")
        builder.append(" : ${importSet.identifier(scenario.baseName)}() {")
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
                val success = renderer.tryRender(builder, identifierContext, ctx, ann)
                if (success) {
                    break
                }
            }
        }
    }

    private fun scenarioType(ctx: RenderContext, type: RunnableScenarioMethodType) {
        annotation(ctx, type.kotlinName) {
            when (type) {
                is AdapterTestMethodType -> {
                    if (type.annotation.expectSkip) {
                        +"expectSkip = true"
                    }
                }

                is EacMethodType -> {
                    +type.annotation.summary.literal
                    type.annotation.notes.forEach { +it.literal }
                }
            }
        }
    }

    private fun beh(ctx: RenderContext, beh: BEH) {
        annotation(ctx, BEH::class.toQualifiedIdentifier()) {
            +identifierContext.behavior(beh.behavior)
            +identifierContext.functionalElement(beh.functionalElement)
            +beh.summary.literal
        }
    }

    internal fun annotation(ctx: RenderContext, type: QualifiedIdentifier, body: AnnotationBuilder.() -> Unit = {}) {
        builder.append("${ctx.indent}@${identifier(type)}")
        AnnotationBuilder(identifierContext, builder).apply(body).end()
        builder.appendLine()
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

                renderer.importSet.imports.joinTo(this, "\n", postfix = "\n\n") { "import $it" }

                appendLine(renderer.builder)
            }
        }
    }
}


