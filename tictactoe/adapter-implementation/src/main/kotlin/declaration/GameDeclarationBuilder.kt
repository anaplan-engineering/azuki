package declaration

import ExecutionEnvironment
import Game
import PlayOrder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration

class GameDeclarationBuilder(override val declaration: GameDeclaration) : SampleDeclarationBuilder<GameDeclaration> {

    override fun build(env: ExecutionEnvironment) {
        val playOrder = env.get<PlayOrder>(declaration.orderName)
        val game = Game.new(3, 3, *playOrder.toTypedArray())
        declaration.moves.map { (pos, sym) -> game.move(toPlayer(sym), pos.col, pos.row) }
        env.add(declaration.name, game)
    }
}
