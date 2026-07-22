package com.anaplan.engineering.azuki.reflect.metadata

import kotlin.test.*

class ImportableTest {

    @Test
    fun wildcard() = with(Importable.wildcard("com.example")) {
        assertEquals("com.example.*", toString())
        assertEquals("com.example.*", import)
        assertEquals("com.example", packageName)
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
}
