import mx.ecosur.multigame.session.GenteSharedBoardTest
import mx.ecosur.multigame.impl.entity.gente.GenteGame
import mx.ecosur.multigame.impl.entity.gente.GenteMove
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.Move;

scenario "first-token", {
    given "an initialized GenteSharedBoardTest", {
      test = new GenteSharedBoardTest ();
      test.fixtures();

      // setup variables
      board = test.board;
      alice = test.alice;
      center = test.center;
      gameId = test.gameId;
      
    }

	when "the-first-player positions a token", {
      game = (GenteGame) board.getGame(gameId).getImplementation();
	  move = new GenteMove (alice, center);
      move = (board.doMove(new Game (game), new Move (move))).getImplementation();
	  game = (GenteGame) board.getGame(gameId).getImplementation();
	}
	then "it must be in the center of the board", {	
	  game.getGrid().getLocation(move.getDestinationCell()) != null;	
	}
}

