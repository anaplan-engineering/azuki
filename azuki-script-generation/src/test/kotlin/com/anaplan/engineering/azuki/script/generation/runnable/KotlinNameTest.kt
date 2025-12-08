package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.script.generation.runnable.KotlinName.Companion.toClassName
import kotlin.test.*

class KotlinNameTest {

    @Test
    fun basic() = with(KotlinName.create("com.example", "Test")) {
        expect("com.example.Test") { fullName }
        expect("com.example.Test") { import }
        expect("com.example") { packageName }
        expect("Test") { identifier }
        assertTrue { hasImport }
        assertTrue { hasPackageName }
        assertTrue { hasIdentifier }
    }

    @Test
    fun basicNested() = with(KotlinName.create("com.example", "Foo") dot "Bar") {
        expect("com.example.Foo.Bar") { fullName }
        expect("com.example.Foo") { import }
        expect("com.example") { packageName }
        expect("Foo.Bar") { identifier }
        assertTrue { hasImport }
        assertTrue { hasPackageName }
        assertTrue { hasIdentifier }
    }


    @Test
    fun arbitrary() = with(KotlinName.generateArbitrary("com.example")) {
        // we can't assume much about what the simple name is!
        assertTrue { fullName.startsWith("com.example.") }
        assertTrue { import.startsWith("com.example.") }
        expect("com.example") { packageName }
        assertTrue { identifier.isNotBlank() }
        assertTrue { hasImport }
        assertTrue { hasPackageName }
        assertTrue { hasIdentifier }
    }

    @Test
    fun wildcard() = with(KotlinName.wildcard("com.example")) {
        expect("com.example.*") { fullName }
        expect("com.example.*") { import }
        expect("com.example") { packageName }
        assertTrue { hasImport }
        assertTrue { hasPackageName }
        assertFalse { hasIdentifier }
    }

    @Test
    fun javaClass() = with(KotlinNameTest::class.toClassName()) {
        expect("com.anaplan.engineering.azuki.script.generation.runnable.KotlinNameTest") { fullName }
        expect("com.anaplan.engineering.azuki.script.generation.runnable.KotlinNameTest") { import }
        expect("com.anaplan.engineering.azuki.script.generation.runnable") { packageName }
        expect("KotlinNameTest") { identifier }
        assertTrue { hasImport }
        assertTrue { hasIdentifier }
    }

    @Test
    fun satisfiesImportDirectly() {
        val foo1 = KotlinName.create("com.example", "foo")
        val foo2 = KotlinName.create("com.example", "foo")
        val bar = KotlinName.create("com.example", "bar")

        assertTrue { foo1.satisfiesImport(foo2) }
        assertFalse { foo1.satisfiesImport(bar) }
    }

    @Test
    fun satisfiesImportWildcard() {
        val pkg = KotlinName.wildcard("com.example")
        val class1 = KotlinName.create("com.example", "foo")
        val class2 = KotlinName.generateArbitrary("com.example")

        assertTrue { pkg.satisfiesImport(class1) }
        assertTrue { pkg.satisfiesImport(class2) }
        assertTrue { pkg.satisfiesImport(pkg) }
        assertFalse { class1.satisfiesImport(pkg) }
        assertFalse { class2.satisfiesImport(pkg) }
    }
}
