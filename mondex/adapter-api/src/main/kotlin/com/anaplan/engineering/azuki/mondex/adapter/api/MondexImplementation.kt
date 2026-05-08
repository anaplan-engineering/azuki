package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults

interface MondexImplementation<A: Action> : Implementation<
    MondexActionFactory<A>,
    MondexCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    NoSystemDefaults> {
}
