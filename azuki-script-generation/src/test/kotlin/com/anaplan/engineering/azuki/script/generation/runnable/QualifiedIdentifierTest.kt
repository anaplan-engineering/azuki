package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier
import kotlin.test.*

class QualifiedIdentifierTest {

    @Test
    fun basic() = with(QualifiedIdentifier.create("com.example", "Test")) {
        assertEquals("com.example.Test", toString())
        assertEquals("com.example.Test", import)
        assertEquals("com.example", packageName)
        assertEquals("Test", identifier)
    }

    @Test
    fun basicNested() = with(QualifiedIdentifier.create("com.example", "Foo") dot "Bar") {
        assertEquals("com.example.Foo.Bar", toString())
        assertEquals("com.example.Foo", import)
        assertEquals("com.example", packageName)
        assertEquals("Foo.Bar", identifier)
    }

    @Test
    fun wildcard() = with(Importable.wildcard("com.example")) {
        assertEquals("com.example.*", toString())
        assertEquals("com.example.*", import)
        assertEquals("com.example", packageName)
    }

    @Test
    fun javaClass() = with(QualifiedIdentifierTest::class.toQualifiedIdentifier()) {
        assertEquals("com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifierTest", toString())
        assertEquals("com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifierTest", import)
        assertEquals("com.anaplan.engineering.azuki.script.generation.runnable", packageName)
        assertEquals("QualifiedIdentifierTest", identifier)
    }

    @Test
    fun canBeUsedToImportDirectly() {
        val foo1 = QualifiedIdentifier.create("com.example", "foo")
        val foo2 = QualifiedIdentifier.create("com.example", "foo")
        val bar = QualifiedIdentifier.create("com.example", "bar")

        assertTrue(foo1 canBeUsedToImport foo2)
        assertFalse(foo1 canBeUsedToImport bar)
    }

    @Test
    fun canBeUsedToImportWildcard() {
        val pkg = Importable.wildcard("com.example")
        val class1 = QualifiedIdentifier.create("com.example", "Foo")
        val class2 = QualifiedIdentifier.create("com.example", "Bar") dot "Baz"

        assertTrue(pkg canBeUsedToImport class1)
        assertTrue(pkg canBeUsedToImport class2)
        assertTrue(pkg canBeUsedToImport pkg)
        assertFalse(class1 canBeUsedToImport pkg)
        assertFalse(class2 canBeUsedToImport pkg)
    }

    @Test
    fun importSetAddIgnoresRedundantImports() {
        val set = ImportSet(
            Importable.wildcard("foo.bar"),
        )
        set += listOf(
            QualifiedIdentifier.create("foo.bar", "baz"),
            QualifiedIdentifier.create("foo.bar", "quux"),
            Importable.wildcard("foo"),
        )
        assertEquals(listOf("foo.*", "foo.bar.*"), set.imports)
    }

    @Test
    fun importSetAddCanRemoveSubsumedImports() {
        val set = ImportSet(
            QualifiedIdentifier.create("foo.bar", "baz"),
            QualifiedIdentifier.create("foo.bar", "quux"),
        )
        set += listOf(
            Importable.wildcard("foo.bar"),
            Importable.wildcard("foo"),
        )
        assertEquals(listOf("foo.*", "foo.bar.*"), set.imports)
    }

    @Test
    fun importSetInitialImportsCanWiden() {
        val widen = ImportSet(
            QualifiedIdentifier.create("foo.bar", "baz"),
            Importable.wildcard("foo.bar"),
        )
        assertEquals(listOf("foo.bar.*"), widen.imports)
    }

    @Test
    fun importSetInitialImportsCanDiscard() {
        val discard = ImportSet(
            Importable.wildcard("foo.bar"),
            QualifiedIdentifier.create("foo.bar", "baz"),
        )
        assertEquals(listOf("foo.bar.*"), discard.imports)
    }
}
