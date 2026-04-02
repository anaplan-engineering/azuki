package com.anaplan.engineering.azuki.reflect.metadata

/**
 * Tracks importable names to allow building a sorted import set.
 *
 * This is somewhat similar to what KotlinPoet does for type names, but as a separate component.
 */
class ImportSet(vararg initialImportables: Importable) : Collection<Importable> {

    private var importables = mutableListOf<Importable>()

    init {
        // Make sure we do filtering on the initial imports
        this += initialImportables.toList()
    }

    /**
     * Gets the sorted imports tracked by this set.
     */
    val imports get() = importables.map { it.import }.sorted()

    /**
     * Adds multiple importable names, if they aren't already tracked.
     */
    operator fun plusAssign(imports: Collection<Importable>) {
        imports.forEach { this += it }
    }

    /**
     * Adds an importable name, if it isn't already tracked.
     */
    operator fun plusAssign(importable: Importable) {
        // Don't add if the import is already tracked...
        if (importables.none { it canBeUsedToImport importable }) {
            // ...and remove anything that is a more narrow equivalent of this import
            importables.removeAll { importable canBeUsedToImport it }
            importables.add(importable)
        }
    }

    override val size get() = importables.size
    override fun contains(element: Importable) = importables.contains(element)
    override fun containsAll(elements: Collection<Importable>) = importables.containsAll(elements)
    override fun isEmpty() = importables.isEmpty()
    override fun iterator() = importables.sortedBy { it.import }.iterator()
}
