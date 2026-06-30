package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class SystemValidCheck : SampleCheck {

    override val behavior = unsupportedBehavior
    override fun check(env: ExecutionEnvironment) = true
}
