package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment
import org.slf4j.LoggerFactory

class DafnyCheckFactory : TicTacToeCheckFactory {
    override val player = DafnyPlayerCheckFactory
    override val game = DafnyGameCheckFactory

    override fun sanityCheck() = DafnySanityCheck
}

object DafnyPlayerCheckFactory : PlayerCheckFactory {
    override fun moveCount(gameName: String, playerName: String, times: Int) = UnsupportedCheck
    override fun cannotPlaceToken(gameName: String, playerName: String, position: Position) = UnsupportedCheck
    override fun hasWon(gameName: String, playerName: String) = UnsupportedCheck
    override fun hasLost(gameName: String, playerName: String) = UnsupportedCheck
}

object DafnyGameCheckFactory : GameCheckFactory {
    override fun hasPlayOrder(gameName: String, players: List<String>) = UnsupportedCheck
    override fun hasState(gameName: String, moves: MoveMap) = UnsupportedCheck
    override fun isComplete(gameName: String) = UnsupportedCheck
    override fun isDraw(gameName: String) = UnsupportedCheck
}

interface DafnyCheck : Check {
    fun check(env: ExecutionEnvironment): Boolean

    fun checkEqual(expected: Any, actual: Any): Boolean {
        val equal = expected == actual
        if (!equal) {
            Log.error("Equality check failed: expected=$expected actual=$actual")
        }
        return equal
    }

    companion object {
        private val Log = LoggerFactory.getLogger(DafnyCheck::class.java)
    }
}

object DafnySanityCheck : DafnyCheck {
    override fun check(env: ExecutionEnvironment) = true
    override val behavior: Behavior = -1
}

val toDafnyCheck: (Check) -> DafnyCheck = {
    @Suppress("UNCHECKED_CAST")
    it as? DafnyCheck ?: throw IllegalArgumentException("Invalid check: $it")
}
