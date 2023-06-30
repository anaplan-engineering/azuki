package com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration

data class GraphDeclaration<T>(
    override val name: String,
    val vertices: Set<T> = emptySet(),
    val edges: Set<Pair<T, T>> = emptySet(),
    override val standalone: Boolean,
): Declaration
