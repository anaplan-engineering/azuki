package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.*

interface GraphImplementation<A: Action>: Implementation<
    GraphActionFactory<A>,
    GraphCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    NoSystemDefaults> {
}
