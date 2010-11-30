import mx.ecosur.multigame.session.ManantialesSharedBoardTest
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove
import mx.ecosur.multigame.model.Move
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame
import mx.ecosur.multigame.model.interfaces.Game
import mx.ecosur.multigame.impl.Color
import mx.ecosur.multigame.impl.model.GridCell
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha
import mx.ecosur.multigame.model.interfaces.Game;

/*
 *   A set of EasyB scenarios to test and verify Manantiales story lines.
 *
 *   @author (awaterma@ecosur.mx)
 */

scenario "constraint relieved", {

  given "A fired check constraint", {

  }

  when "the constraint is relieved", {

  }

  then "the constraint should be clear", {

  }
}

scenario "constraint not relieved", {

  given "A fired check constraint", {
    test = new ManantialesSharedBoardTest ()
    test.fixtures();
    board = test.board
    alice = test.alice
    bob = test.bob
    charlie = test.charlie
    game = board.getGame(test.gameId)

    ficha = new ManantialesFicha (4,3, alice.getColor(),TokenType.MODERATE_PASTURE)

    move = new ManantialesMove (alice, ficha)
    mv = board.doMove(game, new Move (move))

    ficha = new ManantialesFicha (4,5, bob.getColor(), TokenType.MODERATE_PASTURE)
    move = new ManantialesMove (bob, ficha)
    mv = board.doMove(game, new Move (move))

    ficha = new ManantialesFicha (3,4, charlie.getColor(),TokenType.MODERATE_PASTURE)
    move = new ManantialesMove (charlie, ficha)
    mv = board.doMove(game, new Move (move))
  }

  and "the initiator has the turn", {

    charlie.setTurn(true)
  }

  when "he moves w/o relieving the constraint", {
    ficha = new ManantialesFicha (7, 4, charlie.getColor(),TokenType.MODERATE_PASTURE)
    move = new ManantialesMove (charlie, ficha)
    mv = board.doMove (game, new Move (move))
  }

  then "the consequences should be invoked", {
    game = board.getGame(test.gameId);
    implementation = (Game) game.getImplementation();
    implementation.getGrid().getLocation(new GridCell (3,4, Color.UNKNOWN)) == null
  }
}



