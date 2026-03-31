package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedName.Companion.asQualifiedName
import kotlin.test.*

class QualifiedNameTest {

    @Test
    fun basic() = with(QualifiedName.create("com.example", "Test")) {
        assertEquals("com.example.Test", toString())
        assertEquals("com.example.Test", import)
        assertEquals("com.example", packageName)
        assertEquals("Test", simpleName)
    }

    @Test
    fun basicNested() = with(QualifiedName.create("com.example", "Foo") dot "Bar") {
        assertEquals("com.example.Foo.Bar", toString())
        assertEquals("com.example.Foo", import)
        assertEquals("com.example", packageName)
        assertEquals("Foo.Bar", simpleName)
    }

    @Test
    fun wildcard() = with(Importable.wildcard("com.example")) {
        assertEquals("com.example.*", toString())
        assertEquals("com.example.*", import)
        assertEquals("com.example", packageName)
    }

    @Test
    fun javaClass() = with(QualifiedNameTest::class.asQualifiedName()) {
        assertEquals("com.anaplan.engineering.azuki.script.generation.runnable.QualifiedNameTest", toString())
        assertEquals("com.anaplan.engineering.azuki.script.generation.runnable.QualifiedNameTest", import)
        assertEquals("com.anaplan.engineering.azuki.script.generation.runnable", packageName)
        assertEquals("QualifiedNameTest", simpleName)
    }

    @Test
    fun canBeUsedToImportDirectly() {
        val foo1 = QualifiedName.create("com.example", "foo")
        val foo2 = QualifiedName.create("com.example", "foo")
        val bar = QualifiedName.create("com.example", "bar")

        assertTrue(foo1 canBeUsedToImport foo2)
        assertFalse(foo1 canBeUsedToImport bar)
    }

    @Test
    fun canBeUsedToImportWildcard() {
        val pkg = Importable.wildcard("com.example")
        val class1 = QualifiedName.create("com.example", "Foo")
        val class2 = QualifiedName.create("com.example", "Bar") dot "Baz"

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
            QualifiedName.create("foo.bar", "baz"),
            QualifiedName.create("foo.bar", "quux"),
            Importable.wildcard("foo"),
        )
        assertEquals(listOf("foo.*", "foo.bar.*"), set.imports)
    }

    @Test
    fun importSetAddCanRemoveSubsumedImports() {
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
    fun importSetInitialImportsCanWiden() {
        val widen = ImportSet(
            QualifiedName.create("foo.bar", "baz"),
            Importable.wildcard("foo.bar"),
        )
        assertEquals(listOf("foo.bar.*"), widen.imports)
    }

    @Test
    fun importSetInitialImportsCanDiscard() {
        val discard = ImportSet(
            Importable.wildcard("foo.bar"),
            QualifiedName.create("foo.bar", "baz"),
        )
        assertEquals(listOf("foo.bar.*"), discard.imports)
    }
}
