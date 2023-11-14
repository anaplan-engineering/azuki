package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki

import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation

class KazukiTicTacToeImplementation : TicTacToeImplementation {
    override val name = "Kazuki"

    override val total = false

    override val implementationDefaults = NoSystemDefaults

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = KazukiSystemFactory()

}
