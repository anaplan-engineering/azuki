package com.anaplan.engineering.azuki.tictactoe.implementation

import org.junit.Test
import kotlin.test.assertEquals

class TestGameV2 {

    @Test
    fun emptyBoard() {
        val player1 = Player("1", Token.Cross)
        val player2 = Player("1", Token.Circle)
        val expected = """
            . | . | .
            . | . | .
            . | . | .
        """.trimIndent()
        assertEquals(expected, GameV2(3, 3, player1, player2).toString())
    }

    @Test
    fun simpleMove() {
        val player1 = Player("1", Token.Cross)
        val player2 = Player("1", Token.Circle)
        val game = GameV2(3, 5, player1, player2)

        game.move(player1, x = 2, y = 1)
        game.move(player2, x = 1, y = 0)
        game.move(player1, x = 1, y = 2)
        val expected = """
            . | O | . | . | .
            . | . | X | . | .
            . | X | . | . | .
        """.trimIndent()
        assertEquals(expected, game.toString())
    }
}
