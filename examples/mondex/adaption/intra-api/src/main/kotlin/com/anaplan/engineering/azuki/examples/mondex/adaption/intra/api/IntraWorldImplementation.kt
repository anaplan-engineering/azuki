package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults

interface IntraWorldImplementation<A: Action> : Implementation<
    IntraWorldActionFactory<A>,
    IntraWorldCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    NoSystemDefaults> {
}
