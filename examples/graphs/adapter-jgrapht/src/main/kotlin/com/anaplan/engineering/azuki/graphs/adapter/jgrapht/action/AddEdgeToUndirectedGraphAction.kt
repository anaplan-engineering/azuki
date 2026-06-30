package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeToUndirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class AddEdgeToUndirectedGraphAction<V>(graphName: String, source: V, target: V) :
    AddEdgeToUndirectedGraphDeclarableAction<V>(graphName, source, target), JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            //TODO -- add adapter test to check this can be added, will have to add further functionality to ExecutionEnviroment
            //if (source == target) {
            //GraphTypeBuilder.forGraph(this).allowingSelfLoops(true).buildGraph()
            //}
            if (source !in vertexSet()) {
                addVertex(source)
            }
            if (target !in vertexSet()) {
                addVertex(target)
            }
            addEdge(source, target)
        }
    }
}
