package com.anaplan.engineering.azuki.graphs.dsl.declaration

import com.anaplan.engineering.azuki.graphs.dsl.DirectedGraphBlock
import com.anaplan.engineering.azuki.graphs.dsl.UndirectedGraphBlock

interface UndirectedGraphDeclarations {

    fun thereIsAnUndirectedGraph(graphName: String)

    fun thereIsAnUndirectedGraph(graphName: String, init: UndirectedGraphBlock.() -> Unit)

}

interface DirectedGraphDeclarations {

    fun thereIsADirectedGraph(graphName: String)

    fun thereIsADirectedGraph(graphName: String, init: DirectedGraphBlock.() -> Unit)

}
