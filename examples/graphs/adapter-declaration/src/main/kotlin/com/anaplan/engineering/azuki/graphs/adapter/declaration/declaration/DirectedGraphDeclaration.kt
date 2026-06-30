package com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration

data class DirectedGraphDeclaration<T>(
    override val name: String,
    override val vertices: Set<T> = emptySet(),
    override val edges: Set<Pair<T, T>> = emptySet(),
    override val standalone: Boolean,
) : GraphDeclaration<T>
