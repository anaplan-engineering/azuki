package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier
import kotlin.test.*

class QualifiedIdentifierTest {

    @Test
    fun basic() = with(QualifiedIdentifier.create("com.example", "Test")) {
        expect("com.example.Test") { fullName }
        expect("com.example.Test") { import }
        expect("com.example") { packageName }
        expect("Test") { identifier }
    }

    @Test
    fun basicNested() = with(QualifiedIdentifier.create("com.example", "Foo") dot "Bar") {
        expect("com.example.Foo.Bar") { fullName }
        expect("com.example.Foo") { import }
        expect("com.example") { packageName }
        expect("Foo.Bar") { identifier }
    }

    @Test
    fun wildcard() = with(Importable.wildcard("com.example")) {
        expect("com.example.*") { fullName }
        expect("com.example.*") { import }
        expect("com.example") { packageName }
    }

    @Test
    fun javaClass() = with(QualifiedIdentifierTest::class.toQualifiedIdentifier()) {
        expect("com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifierTest") { fullName }
        expect("com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifierTest") { import }
        expect("com.anaplan.engineering.azuki.script.generation.runnable") { packageName }
        expect("QualifiedIdentifierTest") { identifier }
    }

    @Test
    fun canBeUsedToImportDirectly() {
        val foo1 = QualifiedIdentifier.create("com.example", "foo")
        val foo2 = QualifiedIdentifier.create("com.example", "foo")
        val bar = QualifiedIdentifier.create("com.example", "bar")

        assertTrue { foo1 canBeUsedToImport foo2 }
        assertFalse { foo1 canBeUsedToImport bar }
    }

    @Test
    fun canBeUsedToImportWildcard() {
        val pkg = Importable.wildcard("com.example")
        val class1 = QualifiedIdentifier.create("com.example", "Foo")
        val class2 = QualifiedIdentifier.create("com.example", "Bar") dot "Baz"

        assertTrue { pkg canBeUsedToImport class1 }
        assertTrue { pkg canBeUsedToImport class2 }
        assertTrue { pkg canBeUsedToImport pkg }
        assertFalse { class1 canBeUsedToImport pkg }
        assertFalse { class2 canBeUsedToImport pkg }
    }
}
