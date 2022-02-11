package com.anaplan.engineering.azuki.tictactoe.adapter.vdm

import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation

class VdmTicTacToeImplementation: TicTacToeImplementation {

    override val name = "VDM"

    override val total: Boolean = true

    override val implementationDefaults = NoSystemDefaults

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = VdmSystemFactory()
}
