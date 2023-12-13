package com.anaplan.engineering.azuki.tictactoe.adapter.vdm

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation

class VdmTicTacToeImplementation: TicTacToeImplementation {

    override val name = "VDM"

    override val implementationDefaults = NoSystemDefaults

    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = VdmSystemFactory()
}
