package com.anaplan.engineering.azuki.tictactoe.implementation

import org.slf4j.LoggerFactory

class Game(
    val board: MutableList<MutableList<Token?>>,
    val playOrder: PlayOrder,
) {

    val width by lazy { board[0].size }
    val height by lazy { board.size }

    private val lines by lazy {
        mutableListOf<List<Pair<Int, Int>>>().apply {
            addAll((0 until height).map { y -> (0 until width).map { x -> y to x } })
            addAll((0 until width).map { x -> (0 until height).map { y -> y to x } })
            if (width == height) {
                add((0 until width).map { i -> i to i })
                add((0 until width).map { i -> width - 1 - i to i })
            }
        }
    }

    private fun getWinningLine() =
        lines.find { line ->
            line.map { board[it.first][it.second] }.toSet().singleOrNull() != null
        }

    private val winner: Token?
        get() {
            val line = getWinningLine()
            val arbitraryPositionOnLine = line?.first()
            return if (arbitraryPositionOnLine == null) {
                null
            } else {
                board[arbitraryPositionOnLine.first][arbitraryPositionOnLine.second]
            }
        }

    override fun toString() =
        board.joinToString("\n") { row ->
            row.joinToString(" | ") { it?.symbol ?: "." }
        }

    fun move(player: Player, x: Int, y: Int) {
        Log.info("$player moves to (row $y, col=$x)")
        if (canMove(player, x, y)) {
            throw IllegalArgumentException("Coordinates out of bounds (row $y, col=$x)")
        }
        board[y][x] = player.token
    }

    fun canMove(player: Player, x: Int, y: Int): Boolean {
        if (!inBounds(x, y)) {
            return false
        }
        if (board[y][x] != null) {
            return false
        }
        val playerMoveCount = playerMoveCount(player)
        val otherPlayerMoveCount = playerMoveCount(playOrder.filter { it != player }.single())
        return if (player == playOrder.first()) {
            playerMoveCount == otherPlayerMoveCount
        } else {
            playerMoveCount == otherPlayerMoveCount - 1
        }
    }

    private fun inBounds(x: Int, y: Int) = x < 0 || y < 0 || x >= width || y >= height

    fun playerMoveCount(player: Player): Int = board.flatten().count { it == player.token }

    fun hasWon(player: Player) = winner == player.token

    fun hasLost(player: Player) = winner != null && winner != player.token

    fun isComplete() = winner != null || allSpacesFilled()

    fun isDrawn() = allSpacesFilled() && winner == null

    private fun allSpacesFilled() = board.all { it.all { it != null } }

    companion object {
        private val Log = LoggerFactory.getLogger(Game::class.java)

        fun new(rows: Int, cols: Int, vararg player: Player): Game {
            val board = (1..rows).map { (1..cols).map { null }.toMutableList<Token?>() }.toMutableList()
            return Game(board, player.toList())
        }
    }
}

typealias PlayOrder = List<Player>
