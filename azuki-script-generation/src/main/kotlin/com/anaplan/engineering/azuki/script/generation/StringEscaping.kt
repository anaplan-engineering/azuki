package com.anaplan.engineering.azuki.script.generation

/**
 * Gets a potentially-escaped literal form of this string.
 */
val String.literal get() = escapeMultiline().let { esc -> if (esc != this) "$triple$esc$triple" else "\"$this\"" }

/**
 * Escapes this string in a form that is suitable for including in a multiline string literal.
 */
fun String.escapeMultiline() = escapes.fold(this) { self, (lhs, rhs) -> self.replace(lhs, rhs) }

/**
 * Shorthand for triple quote.
 */
const val triple = "\"\"\""

/**
 * Shorthand for escaped triple quote.
 */
const val escapedTriple = "\${$triple}"

private val escapes = listOf(
    "$" to "\${'$'}",
    triple to escapedTriple,
    "\n" to "\${'\\n'}",
    "\r" to "\${'\\r'}",
)
