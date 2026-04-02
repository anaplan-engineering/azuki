package com.anaplan.engineering.azuki.reflect.metadata

import kotlin.test.*

class ImportSetTest {

    @Test
    fun addIgnoresRedundantImports() {
        val set = ImportSet(
            Importable.wildcard("foo.bar"),
        )
        set += listOf(
            QualifiedName.create("foo.bar", "baz"),
            QualifiedName.create("foo.bar", "quux"),
            Importable.wildcard("foo"),
        )
        assertEquals(listOf("foo.*", "foo.bar.*"), set.imports)
    }

    @Test
    fun addCanRemoveSubsumedImports() {
        val set = ImportSet(
            QualifiedName.create("foo.bar", "baz"),
            QualifiedName.create("foo.bar", "quux"),
        )
        set += listOf(
            Importable.wildcard("foo.bar"),
            Importable.wildcard("foo"),
        )
        assertEquals(listOf("foo.*", "foo.bar.*"), set.imports)
    }

    @Test
    fun initialImportsCanWiden() {
        val widen = ImportSet(
            QualifiedName.create("foo.bar", "baz"),
            Importable.wildcard("foo.bar"),
        )
        assertEquals(listOf("foo.bar.*"), widen.imports)
    }

    @Test
    fun initialImportsCanDiscard() {
        val discard = ImportSet(
            Importable.wildcard("foo.bar"),
            QualifiedName.create("foo.bar", "baz"),
        )
        assertEquals(listOf("foo.bar.*"), discard.imports)
    }
}
