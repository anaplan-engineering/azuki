package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier
import kotlin.test.*

class QualifiedIdentifierTest {

    @Test
    fun basic() = with(QualifiedIdentifier.create("com.example", "Test")) {
        assertEquals("com.example.Test", fullName)
        assertEquals("com.example.Test", import)
        assertEquals("com.example", packageName)
        assertEquals("Test", identifier)
    }

    @Test
    fun basicNested() = with(QualifiedIdentifier.create("com.example", "Foo") dot "Bar") {
        assertEquals("com.example.Foo.Bar", fullName)
        assertEquals("com.example.Foo", import)
        assertEquals("com.example", packageName)
        assertEquals("Foo.Bar", identifier)
    }

    @Test
    fun wildcard() = with(Importable.wildcard("com.example")) {
        assertEquals("com.example.*", fullName)
        assertEquals("com.example.*", import)
        assertEquals("com.example", packageName)
    }

    @Test
    fun javaClass() = with(QualifiedIdentifierTest::class.toQualifiedIdentifier()) {
        assertEquals("com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifierTest", fullName)
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
}
