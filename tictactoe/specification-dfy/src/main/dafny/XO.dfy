module com.anaplan.engineering.azuki.tictactoe.specification.dafny.XO {
  import opened PlayOrder
  import opened Player
  import opened Moves

  method createPlayOrder(s: seq<Player>) returns (po: PlayOrder) {
    expect PlayOrderValid(s);
    po := s;
  }

  method createPos(row: nat, col: nat) returns (pos: Pos) {
    expect CoordValid(row) && CoordValid(col);
    pos := Pos(row, col);
  }
}

module com.anaplan.engineering.azuki.tictactoe.specification.dafny.Game {
  import opened Player
  import opened PlayOrder
  import opened Moves
  import opened Board

  class {:autocontracts} Game {
    const board: Board
    const order : PlayOrder

    constructor (newBoard: Board, newOrder: PlayOrder)
    {
      board := newBoard;
      order := newOrder;
    }

    predicate Valid() {
      forall i :: 0 <= i < |order| - 1 ==>
        var current := order[i];
        var next := order[i+1];
        |movesForPlayer(current)| - |movesForPlayer(next)| in {0, 1}
    }

    function movesForPlayer(p: Player): (r: set<Pos>)
      requires |board| <= MAX
      ensures r <= board.Keys
    {
      LemmaAllPosSize();
      set pos | pos in (board.Keys * AllPos) && board[pos] == p
    }

    function movesSoFar(): set<Pos> { board.Keys }

    function moveCountSoFar(): (count: nat)
      ensures count <= MAX
    {
      LemmaBoardSize(board);
      |movesSoFar()|
    }

    function moveCountLeft(): nat { MAX - moveCountSoFar() }
  }
}

module com.anaplan.engineering.azuki.tictactoe.specification.dafny.Board {
  import opened Player
  import opened Moves

  type Board = map<Pos, Player>

  // from standard library
  lemma LemmaSubsetSize<T>(x: set<T>, y: set<T>)
    ensures x < y ==> |x| < |y|
    ensures x <= y ==> |x| <= |y|
  {
    if x != {} {
      var e :| e in x;
      LemmaSubsetSize(x - {e}, y - {e});
    }
  }

  lemma LemmaBoardSize(b: Board)
    ensures |b| <= MAX
  {
    var keys := b.Keys;
    assert forall k :: k in keys ==> k.row <= SIZE && k.col <= SIZE;
    assert keys <= AllPos;
    LemmaSubsetSize(keys, AllPos);
    LemmaAllPosSize();
  }

  const AllPos := set i,j | 1 <= i <= SIZE && 1 <= j <= SIZE :: Pos(i,j)

  lemma LemmaAllPosSize()
    ensures |AllPos| == MAX
}

module com.anaplan.engineering.azuki.tictactoe.specification.dafny.PlayOrder {
  import opened Player

  type PlayOrder = po: seq<Player> | PlayOrderValid(po) witness [Nought, Cross]

  predicate PlayOrderValid(po: seq<Player>) {
    // TODO: Cannot check validity of PlayOrder witness with:
    // forall p: Player :: p in po
    forall p :: p in PLAYERS <==> p in po
  }
}

module com.anaplan.engineering.azuki.tictactoe.specification.dafny.Player {
  
  datatype Player = Nought | Cross

  // TODO: Cannot check validity of PlayOrder witness with:
  // const PLAYERS := set p: Player
  const PLAYERS := {Nought, Cross}
}

module com.anaplan.engineering.azuki.tictactoe.specification.dafny.Moves {

  import opened Player

  const SIZE := 3
  const MAX := SIZE * SIZE

  type coord = c: nat | CoordValid(c) witness 1
  predicate CoordValid(c: nat) { 0 < c <= SIZE }

  datatype Pos = Pos(row: coord, col: coord)

  type Moves = ms: seq<Pos> |
    (forall i, j :: 0 <= i < j < |ms| ==> ms[i] != ms[j])
    && (|PLAYERS| * (SIZE - 1) < |ms| <= MAX)
    witness [Pos(1, 1), Pos(2, 1), Pos(3, 1), Pos(1, 2), Pos(2, 2), Pos(3, 2)]
  
}