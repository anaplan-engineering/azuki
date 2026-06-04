package com.anaplan.engineering.azuki.rightofway.implementation

class RightOfWayV1 {
}

data class Vector2(val x: Double, val y: Double) {
    // Plus Operator: v1 + v2
    operator fun plus(other: Vector2) = Vector2(this.x + other.x, this.y + other.y)

    // Minus Operator: v1 - v2
    operator fun minus(other: Vector2) = Vector2(this.x - other.x, this.y - other.y)

    // Scalar Multiplication (Vector * Scalar): v1 * 2.0
    operator fun times(scalar: Double) = Vector2(this.x * scalar, this.y * scalar)

    // Scalar Division (Vector / Scalar): v1 / 2.0
    operator fun div(scalar: Double) = Vector2(this.x / scalar, this.y / scalar)

    // Dot Product: v1 dot v2
    infix fun dot(other: Vector2): Double = (this.x * other.x) + (this.y * other.y)

    // Vector Length/Magnitude
    val length: Double get() = kotlin.math.sqrt((x * x) + (y * y))
}

/*
// Usage:
val a = Vector2(1.0, 2.0)
val b = Vector2(3.0, 4.0)

val c = a - b       // Subtraction
val d = a * 2.0     // Scalar scaling
val dotProd = a dot b // Dot product: 11.0

v2 should be

dependencies {
    implementation("dev.romainguy:kotlin-math:1.8.0")
}

import dev.romainguy.kotlinmath.*

fun main() {
    // 1. Initialize vectors
    val v1 = Float3(1.0f, 2.0f, 3.0f)
    val v2 = Float3(4.0f, 5.0f, 6.0f)

    // 2. Vector Subtraction (using standard '-' operator)
    val subtraction = v1 - v2 // Float3(-3.0, -3.0, -3.0)

    // 3. Scalar Multiplication (using standard '*' operator)
    val scalarProduct = v1 * 2.0f // Float3(2.0, 4.0, 6.0)

    // 4. Dot Product (using standard math library function)
    val dotProduct = dot(v1, v2) // Returns Float (32.0f)

    // 5. Length/Magnitude of vector
    val magnitude = length(v1)
}

 */
