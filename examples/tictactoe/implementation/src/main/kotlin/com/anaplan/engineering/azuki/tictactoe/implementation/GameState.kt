package com.anaplan.engineering.azuki.tictactoe.implementation

data class GameState(
    val board: MutableList<MutableList<Token?>>,
    val playOrder: PlayOrder,
)
