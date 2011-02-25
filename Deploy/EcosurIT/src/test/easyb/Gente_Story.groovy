import mx.ecosur.multigame.session.GenteSharedBoardTest
import mx.ecosur.multigame.impl.entity.gente.GenteMove
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.Move
import mx.ecosur.multigame.impl.model.GridCell;

scenario "first-token", {
    given "an initialized GenteSharedBoardTest", {
      test = new GenteSharedBoardTest ()
      test.fixtures()

      // setup variables
      board = test.board
      alice = test.alice
      center = test.center
      gameId = test.gameId
      
    }

    when "the-first-player positions a token", {
      game = board.getGame(gameId).getImplementation()
     move = new GenteMove (alice, center)
      move = (board.doMove(new Game (game), new Move (move))).getImplementation()
      game = board.getGame(gameId).getImplementation()
    }
    then "it must be in the center of the board", {
     cell = game.getGrid().getLocation(move.getDestinationCell())
      cell.shouldBe move.getDestinationCell()
    }
}

scenario "2 alices and 2 bob in a column", {
    given "2 alices in a column on the board", {
      test = new GenteSharedBoardTest ()
      test.fixtures()

      // setup local variables
      board = test.board
      alice = test.alice
      bob = test.bob
      center = test.center
      gameId = test.gameId

      //setup initial conditions
      game = board.getGame(gameId).getImplementation()
      move = new GenteMove (alice, center)
      mv = board.doMove(new Game (game), new Move (move))
      alice.setTurn(true)
      bob.setTurn(false)

      cell = new GridCell (center.getRow() - 1, center.getColumn (), alice.getColor())
      game = board.getGame(gameId).getImplementation()
      move = new GenteMove (alice, cell)
      mv = board.doMove(new Game (game), new Move (move))

    }

    when "bob sets a token on the end", {

      cell = new GridCell (center.getRow() - 2, center.getColumn (), bob.getColor())
      game = board.getGame(gameId).getImplementation()
      move = new GenteMove (bob, cell)
      mv = board.doMove(new Game (game), new Move (move))

      bob.setTurn(true);
      cell = new GridCell (center.getRow() +1, center.getColumn (), bob.getColor())
      game = board.getGame(gameId).getImplementation()
      move = new GenteMove (bob, cell)
      mv = board.doMove(new Game (game), new Move (move))

    }

    then "it must not form a tessera.", {

      move.getTesseras().size().shouldBe 0

    }
}
