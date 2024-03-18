package com.anaplan.engineering.azuki.tictactoe.adapter.dafny

import com.anaplan.engineering.azuki.core.system.Implementation
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation

class DafnyTicTacToeImplementation: TicTacToeImplementation {

    override val name = "Dafny"

    override val implementationDefaults = NoSystemDefaults

    override val versionFilter = Implementation.VersionFilter.DefaultVersionFilter

    override fun createSystemFactory(systemDefaults: NoSystemDefaults) = DafnySystemFactory()
}
