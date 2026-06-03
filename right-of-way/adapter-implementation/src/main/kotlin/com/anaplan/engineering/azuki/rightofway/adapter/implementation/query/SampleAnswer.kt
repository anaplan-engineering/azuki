package com.anaplan.engineering.azuki.rightofway.adapter.implementation.query

import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory

open class SampleAnswer<T> (
    override val to: Query<T>,
    override val value: T,
    private val checkCreator: (RightOfWayCheckFactory) -> List<Check>
) : Answer<T, RightOfWayCheckFactory> {

    override fun createChecks(factory: RightOfWayCheckFactory) = checkCreator(factory)
    override fun toString() = "Answer: $value"
}
