package com.anaplan.engineering.azuki.reflect.metadata

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
    fun javaClass() = with(QualifiedNameTest::class.asQualifiedName()) {
        assertEquals("com.anaplan.engineering.azuki.reflect.metadata.QualifiedNameTest", toString())
        assertEquals("com.anaplan.engineering.azuki.reflect.metadata.QualifiedNameTest", import)
        assertEquals("com.anaplan.engineering.azuki.reflect.metadata", packageName)
        assertEquals("QualifiedNameTest", simpleName)
    }
}
