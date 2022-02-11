# A BDS project example for the game of tic-tac-toe

This project includes a [specification](adapter-vdm/src/main/vdm/XO.vdmsl) based on
the [example tic-tac-toe specification](https://www.overturetool.org/download/examples/VDMSL/Tic-tac-toeSL/index.html)
by Nick Battle available from the Overture Tool website, which was heavily modified following the BDS process described
below.

## Functional elements

- 1 - A game of tic-tac-toe

## Behaviours

The following behaviours have been defined

- 1 - Start a new game
- 2 - Get the move count for a given player
- 3 - Place a token
- 4 - Create a play order
- 5 - Get the play order for a given game
- 6 - Complete a game

## The process

1. Write a scenario that exemplifies the desired aspect of the behaviour being specified, either under
   [eacs/.../eacs](eacs/src/test/kotlin/com/anaplan/engineering/azuki/tictactoe/eacs) or
   [eacs/.../analysis](eacs/src/test/kotlin/com/anaplan/engineering/azuki/tictactoe/analysis). You may need to add
   the relevant
   [behaviour](adapter-api/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/api/TicTacToeBehaviours.kt)
   and/or
   [functional element](adapter-api/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/api/TicTacToeFunctionalElements.kt)
   to the `adapter-api` if they have not been defined already. If no new DSL functions are needed, proceed to
   [Updating the specification](#updating-the-specification) below.
2. Add stubs for the required new DSL functions to
   [TicTacToeGiven](dsl/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/dsl/TicTacToeGiven.kt) (pre-conditions),
   [TicTacToeWhen](dsl/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/dsl/TicTacToeWhen.kt) (actions), or
   [TicTacToeThen](dsl/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/dsl/TicTacToeThen.kt) (post-conditions).
3. Add the required API functions to the interfaces in
   [TicTacToeActionFactory](adapter-api/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/api/TicTacToeActionFactory.kt)
   (pre-conditions and actions), or
   [TicTacToeCheckFactory](adapter-api/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/api/TicTacToeCheckFactory.kt)
   (post-conditions). To avoid compilation errors, these can be added to the adapter implementation as `UnsupportedAction`
   in [VdmActionFactory](adapter-vdm/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/vdm/action/VdmActionFactory.kt)
   and [VdmCheckFactory](adapter-vdm/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/vdm/check/VdmCheckFactory.kt),
   respectively.
4. Ensure that the specification exposes the logic required to implement the desired action or check. How this is hooked
   into the adapter depends on whether we are implementing a declaration (pre-condition), an action or a check.

### Declarations

1. If necessary, create a new declaration under
   [adapter-declaration/.../declaration](adapter-declaration/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/declaration/declaration)
   and add it to the
   [createVdmDeclarationBuilder](adapter-vdm/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/vdm/declaration/VdmDeclarationBuilder.kt)
   function.
2. Add a method to the
   [DeclarationBuilder](adapter-declaration/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/declaration/DeclarationBuilder.kt)
   class to either create or modify a declaration, as required. See e.g. `DeclarationBuilder.playerMove`.
3. Add the desired `DeclarableAction` to
   [adapter-declaration/.../action](adapter-declaration/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/declaration/action).
   See e.g.
   [PlayerMoveDeclarableAction](adapter-declaration/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/declaration/action/PlayerMoveDeclarableAction.kt).
4. If the declarable action can only be used as a declaration, add it directly to `VdmActionFactory`. Otherwise add an
   action that inherits from it (see below).

### Actions

Actions should inherit from [VdmAction](../azuki-vdm/src/main/kotlin/com/anaplan/engineering/azuki/vdm/VdmAction.kt) and
override the `build` method. For example, see
[PlayerMoveAction](adapter-vdm/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/vdm/action/PlayerMoveAction.kt).
They should also either inherit from the corresponding
[DeclarableAction](adapter-declaration/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/declaration/DeclarableAction.kt)
(as in the case of `PlayerMoveAction`), or from the appropriate behaviour.

### Checks

Checks should inherit from [VdmCheck](../azuki-vdm/src/main/kotlin/com/anaplan/engineering/azuki/vdm/VdmCheck.kt) and
override the `build` method. For example, see
[BoardHasStateCheck](adapter-vdm/src/main/kotlin/com/anaplan/engineering/azuki/tictactoe/adapter/vdm/check/BoardHasStateCheck.kt).
They should also inherit from the appropriate behaviour.

### Updating the specification

Once a scenario has been written and the adapter and DSL have been updated as required, animate the scenario against the
specification, ensuring it fails. Then make just enough changes to the specification to have it pass, and move on to the
next acceptance criterion.
