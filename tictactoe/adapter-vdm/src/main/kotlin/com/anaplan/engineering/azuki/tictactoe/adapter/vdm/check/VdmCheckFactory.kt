package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.vdm.EmptySystemContext
import com.anaplan.engineering.azuki.vdm.VdmCheck

class VdmCheckFactory : TicTacToeCheckFactory {
    override val player = VdmPlayerCheckFactory
    override val game = VdmGameCheckFactory
}

object VdmPlayerCheckFactory : PlayerCheckFactory {
    override fun moveCount(gameName: String, playerName: String, times: Int) =
        PlayerMoveCountCheck(gameName, playerName, times)
    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) =
        PlayerCannotPlaceTokenCheck(gameName, playerName, position)
    override fun hasWon(gameName: String, playerName: String) = PlayerHasWonCheck(gameName, playerName)
    override fun hasLost(gameName: String, playerName: String) = PlayerHasLostCheck(gameName, playerName)
}

object VdmGameCheckFactory : GameCheckFactory {
    override fun hasPlayOrder(gameName: String, players: List<String>) = GameHasPlayOrderCheck(gameName, players)
    override fun hasState(gameName: String, moves: MoveMap) = BoardHasStateCheck(gameName, moves)
    override fun isComplete(gameName: String) = BoardIsCompleteCheck(gameName)
    override fun isDraw(gameName: String) = GameIsDrawCheck(gameName)
}

interface DefaultVdmCheck : VdmCheck<EmptySystemContext> {

    fun checkEquals(actual: String = "actual", expected: String = "expected", msg: String? = null) =
        """
            if not $actual = $expected
            then return false
            else skip;
        """
}

val toDefaultVdmCheck: (Check) -> DefaultVdmCheck = {
    @Suppress("UNCHECKED_CAST")
    it as? DefaultVdmCheck ?: throw IllegalArgumentException("Invalid check: $it")
}
