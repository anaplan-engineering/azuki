package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.runner.ToBeDone
import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.core.system.ImplementationVersion
import com.anaplan.engineering.azuki.script.generation.RenderContext
import com.anaplan.engineering.azuki.script.generation.literal
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Shorthand for defining an annotation renderer.
 */
inline fun <reified T : Annotation> renderAnnotation(noinline renderBody: AnnotationBuilder.(T) -> Unit) =
    RunnableScenarioAnnotationRenderer(T::class, renderBody)

/**
 * Carries a method-level annotation type and information about how to render it.
 *
 * While Azuki contains several standard method-level annotation types, and defaults to rendering them in a
 * standard manner and ordering, this framework exists to let end users add in script generation functionality for
 * custom method-level scenarios.
 */
data class RunnableScenarioAnnotationRenderer<T : Annotation>(
    val annotationClass: KClass<T>, val renderBody: AnnotationBuilder.(T) -> Unit
) {
    /**
     * Tries to render an annotation of arbitrary type at the top level.
     * Returns whether this renderer could handle this annotation.
     */
    fun tryRender(
        destination: Appendable,
        identifierContext: IdentifierContext,
        renderContext: RenderContext,
        annotation: Annotation,
    ): Boolean {
        // make sure this is the right type of annotation for this renderer
        val annotation = annotationClass.safeCast(annotation) ?: return false

        AnnotationBuilder.renderTopLevelAnnotation(
            destination,
            identifierContext,
            renderContext,
            annotationClass.toQualifiedIdentifier(),
        ) {
            renderBody(annotation)
        }
        return true
    }

    companion object {

        /**
         * Renders a 'KnownBug' annotation.
         */
        val knownBug = renderAnnotation<KnownBug> { a -> a.issues.forEach { issue(it) } }

        /**
         * Renders a 'ToBeDone' annotation.
         */
        val toBeDone = renderAnnotation<ToBeDone> { a -> a.issues.forEach { issue(it) } }

        /**
         * Renders a 'Since' annotation.
         */
        val since = renderAnnotation<Since> { a -> a.implementationVersion.forEach { implementationVersion(it) } }

        /**
         * The standard list of annotation renderers, in approximate decreasing-change-frequency order.
         */
        val standardRenderers = arrayOf(
            knownBug,
            toBeDone,
            since,
        )
    }
}

/**
 * Helper for constructing part or all of an annotation.
 */
class AnnotationBuilder(val identifierContext: IdentifierContext, private val destination: Appendable) {

    private var hasMembers = false

    /**
     * Adds a string into an annotation fragment.
     */
    operator fun String.unaryPlus() {
        member {
            destination.append(this@unaryPlus)
        }
    }

    fun issue(issue: Issue) {
        nested(Issue::class.toQualifiedIdentifier()) {
            +identifierContext.implementation(issue.implementation)
            issue.jiraIds.forEach { +it.literal }
        }
    }

    fun implementationVersion(version: ImplementationVersion) {
        nested(ImplementationVersion::class.toQualifiedIdentifier()) {
            +identifierContext.implementation(version.name)
            +version.version.literal
        }
    }

    /**
     * Handles a nested annotation.
     */
    fun nested(type: QualifiedIdentifier, body: AnnotationBuilder.() -> Unit) {
        member {
            destination.append(identifierContext.identifier(type))
            AnnotationBuilder(identifierContext, destination).apply(body).end()
        }
    }

    private fun member(body: AnnotationBuilder.() -> Unit) {
        destination.append(memberPrefix)
        body()
        hasMembers = true
    }

    internal fun end() {
        if (hasMembers) {
            destination.append(")")
        }
    }

    private val memberPrefix get() = if (hasMembers) ", " else "("

    companion object {

        /**
         * Renders a top-level annotation to an appendable in one go.
         */
        fun renderTopLevelAnnotation(
            destination: Appendable,
            identifierContext: IdentifierContext,
            renderContext: RenderContext,
            type: QualifiedIdentifier,
            body: AnnotationBuilder.() -> Unit = {}
        ) {
            destination.append("${renderContext.indent}@${identifierContext.identifier(type)}")
            AnnotationBuilder(identifierContext, destination).apply(body).end()
            destination.appendLine()
        }
    }
}
