@Eac("If the board is empty, neither player has made a move")
fun emptyBoard() {
    given {
        thereIsABoard(boardA, """
            . | . | .
            . | . | .
            . | . | .
        """)
    }
    then {
        playerHasMoved(X, times = 0)
        playerHasMoved(O, times = 0)
    }
}

@Eac("When a player places a token, the board is updated")
fun turns() {
    given {
        thereIsABoard(boardA, """
            . | . | .
            . | . | .
            . | . | .
        """)
    }
    whenever {
        playerHasTurn(boardA, O, x = 0, y = 0)
        playerHasTurn(boardA, X, x = 2, y = 2)
    }
    then {
        boardHasState(boardA, """
            O | . | .
            . | . | .
            . | . | X
        """)
    }
}

@Eac("A player cannot place a token over an existing token")
fun immutableState() {
    given {
        thereIsABoard(boardA, """
            . | . | .
            . | X | .
            . | . | .
        """)
    }
    then {
        playerCannotHaveTurn(boardA, O, x = 1, y = 1)
    }
}

@Eac("A player can win by completing a line of three tokens")
fun columnWin() {
    given {
        thereIsABoard(boardA, """
            X | O | O
            . | . | .
            X | . | O
        """)
    }
    whenever {
        playerHasTurn(boardA, X, x = 0, y = 1)
    }
    then {
        boardIsComplete(boardA)
        playerHasWon(boardA, X)
        playerHasLost(boardA, O)
    }
}

@Eac("It is possible for the board to be complete without either player having won")
fun draw() {
    given {
        thereIsABoard(boardA, """
            X | O | O
            O | O | X
            X | X | O
        """)
    }
    then {
        boardIsComplete(boardA)
        gameIsDraw(boardA)
    }
}

@Eac("Only one player can win a given game")
fun singleWinner() {
    given {
        thereIsABoard(boardA)
        playerHasWon(boardA, X)
    }
    then {
        playerHasLost(boardA, O)
    }
}
