package com.anaplan.engineering.azuki.graphs.dsl.declaration

import com.anaplan.engineering.azuki.graphs.dsl.GraphBlock

interface GraphDeclarations {

    fun thereIsAGraph(graphName: String)

    fun thereIsAGraph(graphName: String, init: GraphBlock.() -> Unit)

}
