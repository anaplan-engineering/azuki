package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter

/**
 * A scenario script.
 */
abstract class ScenarioScript(val typeName: String, val blocks: ScriptElementList<ScriptBlock>) {

    /**
     * Renders the script.
     *
     * Apply a block to this method to configure the renderer before it renders the script.
     */
    fun render(config: Renderer.() -> Unit = {}) = Renderer().apply(config).render(blocks)

    /**
     * Customisable renderer for scenario scripts.
     */
    inner class Renderer(
        /**
         * The formatter to use, if any.
         */
        var formatter: Formatter = Formatter.Full,
        /**
         * How to convert the scenario's blocks into a script.
         */
        var scriptType: ScriptType = ScriptType.Standalone,
        /**
         * The starting indent level for the renderer.
         */
        var indentLevel: Int = 0,
        /**
         * String to be repeated once for each indent level.  (Usually this will be some multiple of spaces or tabs.)
         */
        var indentString: String = "    "
    ) {

        internal fun render(blocks: ScriptElementList<ScriptBlock>): String {
            val wrapped = scriptType.wrap(typeName, blocks)
            val rendered = wrapped.render(RenderContext(indentLevel, indentString))
            return formatter.format(rendered)
        }
    }

}

/**
 * The type of script output to render.
 */
fun interface ScriptType {

    fun wrap(typeName: String, blocks: ScriptElementList<ScriptBlock>): ScriptElement

    companion object {

        /**
         * Don't wrap the script blocks in an outer function call.
         */
        val Inline = ScriptType { _, blocks -> blocks }

        /**
         * Wrap the script blocks in the appropriate scenario function.
         */
        val Standalone = ScriptType { typeName, blocks -> ScriptBlock("${typeName}Scenario", blocks) }
    }
}

/**
 * The type of formatter to run on the script after rendering.
 */
fun interface Formatter {

    fun format(scenarioScript: String): String

    companion object {

        /**
         * Don't format.
         */
        val None = Formatter { it }

        /**
         * Format the scenario using the full-scenario formatter.
         */
        val Full = Formatter(ScenarioFormatter::formatScenario)
    }
}

/**
 * A verifiable scenario (given-whenever-then) script.
 */
data class VerifiableScenarioScript(
    val given: ScriptBlock, val whenever: ScriptBlock, val then: ScriptBlock
) : ScenarioScript("verifiable", ScriptElementList(given, whenever, then))

/**
 * A query scenario (given-whenever-query) script.
 */
data class QueryScenarioScript(
    val given: ScriptBlock, val whenever: ScriptBlock, val query: ScriptBlock
) : ScenarioScript("query", ScriptElementList(given, whenever, query))

/**
 * An oracle scenario (given-generate-whenever-generate-verify) script.
 */
data class OracleScenarioScript(
    val given: ScriptBlock,
    val givenGenerate: ScriptElementList<ScriptBlock>,
    val whenever: ScriptBlock,
    val whenGenerate: ScriptElementList<ScriptBlock>,
    val verify: ScriptBlock
) : ScenarioScript("oracle", ScriptElementList(buildList {
    add(given)
    addAll(givenGenerate.elements)
    add(whenever)
    addAll(whenGenerate.elements)
    add(verify)
}))

/**
 * A component in a DSL script that is being generated.
 */
interface ScriptElement {

    /**
     * Is the element empty and therefore safe to skip?
     */
    val isEmpty: Boolean

    /**
     * Is the element safe to lay out horizontally?
     */
    val isHorizontal: Boolean get() = false

    /**
     * Renders the contents of the script element to a string with the given context.
     */
    fun render(ctx: RenderContext = RenderContext()): String
}

/**
 * A raw string fragment as a script element.
 *
 * Because we don't know anything about the contents of these elements, we can't inspect them or safely reformat them
 * (aside from passing the end result to a general Kotlin formatter).  This means that they ignore indentation hints,
 * for example.
 */
data class ScriptStringFragment(val fragment: String) : ScriptElement {

    override val isEmpty = fragment.isBlank()
    override val isHorizontal = '\n' !in fragment

    override fun render(ctx: RenderContext) = fragment
}

/**
 * A script element representing a function call.
 */
data class ScriptFunction(val name: String, val args: List<ScriptElement>) : ScriptElement {

    override val isEmpty = false

    override fun render(ctx: RenderContext) = buildString {
        append("${ctx.indent}$name(")

        val anyVertical = args.any { !it.isHorizontal }
        if (anyVertical) {
            appendLine()
        }

        args.forEachIndexed { i, arg ->
            val wasHorizontal = args.getOrNull(i - 1)?.isHorizontal

            val joiner = when {
                i == 0 -> ""
                arg.isHorizontal && wasHorizontal == true -> ", "
                // force a linebreak when transitioning from horizontal to vertical, or between vertical, arguments
                else -> ",\n"
            }

            val argCtx = if (arg.isHorizontal) {
                // we'll handle indenting horizontally laid-out arguments
                ctx.copy(indentLevel = 0)
            } else {
                ctx.nextIndentLevel
            }

            append(joiner)
            val startOfHorizontalRun = anyVertical && arg.isHorizontal && wasHorizontal != true
            if (startOfHorizontalRun) {
                append(ctx.nextIndentLevel.indent)
            }
            append(arg.render(argCtx))
        }

        if (anyVertical) {
            append(",\n${ctx.indent})")
        } else {
            append(")")
        }
    }
}

/**
 * A standard DSL script block with a header.
 */
data class ScriptBlock(val header: String, val inner: ScriptElement) : ScriptElement {

    constructor(header: String, contents: List<ScriptElement>) : this(header, ScriptElementList(contents))

    override val isEmpty get() = inner.isEmpty
    override val isHorizontal = false

    /**
     * Gets the script elements contained within this block.
     */
    val elements get() = if (inner is ScriptElementList<*>) inner.elements else listOf(inner)

    override fun render(ctx: RenderContext) = with(ctx) {
        listOf("$indent$header {", inner.render(nextIndentLevel), "$indent}").joinToString("\n")
    }

    /**
     * A script block with the same header and contents, but with `isEmpty` forced to false.
     */
    val asNonEmpty get() = copy(inner = NonEmpty(inner))
}

/**
 * A script element that wraps another script element, but always reports that it is non-empty.
 */
data class NonEmpty(val inner: ScriptElement) : ScriptElement by inner {

    override val isEmpty: Boolean = false
}

/**
 * A script element containing zero or more vertically joined script elements.
 */
data class ScriptElementList<E : ScriptElement>(val elements: List<E>) : ScriptElement {

    constructor(vararg elements: E) : this(listOf(*elements))

    override val isEmpty get() = elements.all { it.isEmpty }

    override fun render(ctx: RenderContext) = elements.filterNot { it.isEmpty }.joinToString("\n") { it.render(ctx) }

    operator fun plus(other: ScriptElementList<E>) = ScriptElementList(elements + other.elements)
}

/**
 * Context about how to render a script element, which is threaded through the rendering process.
 */
data class RenderContext(
    val indentLevel: Int = 0,
    val indentString: String = "    ",
    // TODO: we might need other things here such as target line width
    // TODO: should this be parameterised on implementation-specific data?
) {

    /**
     * Creates a new context for the next indent level.
     */
    val nextIndentLevel by lazy { copy(indentLevel = indentLevel + 1) }

    /**
     * Indents up to the current indent level.
     */
    val indent by lazy { indentString.repeat(indentLevel) }
}
