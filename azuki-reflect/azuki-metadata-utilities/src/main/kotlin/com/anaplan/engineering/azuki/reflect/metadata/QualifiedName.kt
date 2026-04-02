package com.anaplan.engineering.azuki.reflect.metadata

import kotlin.reflect.KClass

/**
 * Creates a <code>QualifiedName</code> directly from a Kotlin class reference.
 */
fun KClass<*>.asQualifiedName(): QualifiedName = java.asQualifiedName()

/**
 * Creates a <code>QualifiedName</code> directly from a Java class reference.
 */
fun Class<*>.asQualifiedName(): QualifiedName = JavaClassName(this)

/**
 * The fully-qualified name of a Kotlin definition (package, class, constant, and so on).
 *
 * This class hierarchy is mostly used for handling imports and references within runnable scenarios, and so is very
 * loosely defined with little internal resemblance to the JVM's concept of names.
 */
sealed class QualifiedName : Importable() {

    /**
     * The unqualified part of the name, by which the identified object can be referred to when imported.
     */
    abstract val simpleName: String

    /**
     * Creates a nested name.
     *
     * A nested name is imported in the same way as its parent, but has its name qualified by that of the parent.
     */
    infix fun dot(name: String): QualifiedName = NestedName(this, name)

    companion object {

        /**
         * Creates a qualified name directly from a package name and simple name.
         *
         * We make a basic attempt to sanitize or reject any invalid characters.  For instance, whitespace is rejected
         * outright, and hyphens are replaced with underscores.
         */
        fun create(packageName: String, simpleName: String): QualifiedName = BasicQualifiedName(
            packageName.sanitizedForQualifiedName,
            simpleName.sanitizedForQualifiedName,
        )
    }
}


private class BasicQualifiedName(override val packageName: String, override val simpleName: String) : QualifiedName() {

    init {
        packageName.checkValidForQualifiedName()
        simpleName.checkValidForQualifiedName()
    }

    override fun toString() = import
    override val import = "$packageName.$simpleName"
    override fun canBeUsedToImport(other: Importable) = import == other.import
}

private class JavaClassName(jClass: Class<*>) : QualifiedName() {

    // TODO: detect and properly handle nesting?
    override fun toString() = import
    override val import: String = checkNotNull(jClass.canonicalName)
    override val packageName: String = checkNotNull(jClass.packageName)
    override val simpleName: String = checkNotNull(jClass.simpleName)
    override fun canBeUsedToImport(other: Importable) = import == other.import
}

private class NestedName(parent: QualifiedName, child: String) : QualifiedName() {

    init {
        // assume inductively that `parent` is valid
        require(parent.simpleName.isNotEmpty()) { "parent of a nested name must have a simple name" }
        child.checkValidForQualifiedName()
    }

    private val fullName = "$parent.$child"
    override fun toString() = fullName

    override val import = parent.import
    override val packageName = parent.packageName
    override val simpleName = "${parent.simpleName}.$child"
    override fun canBeUsedToImport(other: Importable) = import == other.import
}

internal val String.sanitizedForQualifiedName get() =
    // We're more tolerant of the following, which are less likely to be errors in using QualifiedName
    // and more likely to be passing things in from other parts of a verification-generation setup verbatim
    replace("-", "_")

internal fun String.checkValidForQualifiedName() {
    // NOTE: this is not exhaustive but tries to catch out the most likely issues
    require(isNotBlank()) { "parts of names cannot be blank" }
    require(!contains("*")) { "wildcards not allowed here" }
    require(!contains("-")) { "hyphens not allowed here" }
}
