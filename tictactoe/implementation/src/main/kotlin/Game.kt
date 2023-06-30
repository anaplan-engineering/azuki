class Game(
    val board: MutableList<MutableList<Token?>>,
    val playOrder: PlayOrder,
) {

    val width by lazy { board[0].size }
    val height by lazy { board.size }

    override fun toString() =
        board.joinToString("\n") { row ->
            row.joinToString(" | ") { it?.symbol ?: "." }
        }

    fun move(player: Player, x: Int, y: Int) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw IllegalArgumentException("Coordinates out of bounds")
        }
        board[y][x] = player.token
    }

    companion object {

        fun new(rows: Int, cols: Int, vararg player: Player) : Game {
            val board = (1..rows).map { (1..cols).map { null }.toMutableList<Token?>() }.toMutableList()
            return Game(board, player.toList())
        }
    }
}

typealias PlayOrder = List<Player>
