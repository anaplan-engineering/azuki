package com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration

sealed interface GraphDeclaration<T> : Declaration {
    val vertices: Set<T>
    val edges: Set<Pair<T, T>>
}
