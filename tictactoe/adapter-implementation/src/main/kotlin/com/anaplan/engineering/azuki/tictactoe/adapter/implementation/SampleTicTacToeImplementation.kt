package com.anaplan.engineering.azuki.tictactoe.adapter.implementation

import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation

class SampleTicTacToeImplementation : TicTacToeImplementation {
    override val name = "SampleImpl"

    override val implementationDefaults = NoSystemDefaults

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = SampleSystemFactory()

}
