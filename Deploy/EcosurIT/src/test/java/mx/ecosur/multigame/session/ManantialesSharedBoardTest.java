/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
* @author awaterma@ecosur.mx
*/
package mx.ecosur.multigame.session;

import static org.junit.Assert.*;

import java.rmi.RemoteException;

import java.util.List;

import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sun.appserv.security.ProgrammaticLogin;
import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.exception.InvalidSuggestionException;
import mx.ecosur.multigame.impl.entity.manantiales.*;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ManantialesSharedBoardTest {


    private RegistrarRemote registrar;

    private SharedBoardRemote board;

    private int gameId;

    private ManantialesPlayer alice, bob, charlie, denise;


    @Before
    public void fixtures () throws RemoteException, NamingException, InvalidRegistrationException {
        ProgrammaticLogin login = new ProgrammaticLogin();
        login.login("MultiGame", "test");
        InitialContext ic = new InitialContext();

        registrar = (RegistrarRemote) ic.lookup(
            "mx.ecosur.multigame.ejb.interfaces.RegistrarRemote");

        GridRegistrant[] registrants = {
            new GridRegistrant ("alice"),
            new GridRegistrant ("bob"),
            new GridRegistrant ("charlie"),
            new GridRegistrant ("denise")};

        ManantialesGame game = new ManantialesGame ();
        game.setMode(Mode.CLASSIC);
        Game boardGame = new Game (game);

        for (int i = 0; i < 4; i++) {
            Registrant registrant = registrar.register(new Registrant (registrants [ i ]));
            boardGame = registrar.registerPlayer(boardGame, registrant);
            if (gameId == 0) {
                gameId = boardGame.getId();
            }
        }

        /* Get the SharedBoard */
        board = (SharedBoardRemote) ic.lookup(
                "mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote");
        game = (ManantialesGame) board.getGame(gameId).getImplementation();

        /* Set the GamePlayers from the SharedBoard */
        List<GridPlayer> players = game.getPlayers();
        for (GridPlayer p : players) {
            if (p.getRegistrant().getName().equals("alice"))
                alice = (ManantialesPlayer) p;
            else if (p.getRegistrant().getName().equals("bob"))
                bob = (ManantialesPlayer) p;
            else if (p.getRegistrant().getName().equals("charlie"))
                charlie = (ManantialesPlayer) p;
            else if (p.getRegistrant().getName().equals("denise"))
                denise = (ManantialesPlayer) p;
        }

        assertNotNull ("Alice not found in game!", alice);
        assertNotNull ("Bob not found in game!", bob);
        assertNotNull ("Charlie not found in game!", charlie);
        assertNotNull ("Denise not found in game!", denise);
    }

    @After
    public void tearDown () throws NamingException, RemoteException, InvalidRegistrationException {
        ManantialesGame game = (ManantialesGame) board.getGame(gameId).getImplementation();
        registrar.unregister(new Game (game), new GamePlayer (alice));
        registrar.unregister(new Game (game), new GamePlayer (bob));
        registrar.unregister(new Game (game), new GamePlayer (charlie));
        registrar.unregister(new Game (game), new GamePlayer (denise));
    }


    /**
     * Simple test to determine if there are the correct number of squares
     * after the game state is set to BEGIN.
     * @throws RemoteException
     */
    @Test
    public void testGetGameGrid() throws RemoteException {
        ManantialesGame game = (ManantialesGame) board.getGame(gameId).getImplementation();
        assertTrue (game.getGrid().getCells().size() == 0);
    }

    /** Test on ManantialesGame for setting check constraints
     * @throws InvalidMoveException */
    @Test
    public void testCheckConstraints () throws InvalidMoveException {
        Game game = board.getGame(gameId);
        ManantialesFicha ficha = new ManantialesFicha(4,3, alice.getColor(),
                TokenType.MODERATE_PASTURE);

        ManantialesMove move = new ManantialesMove (alice, ficha);
        Move mv = board.doMove(game, new Move (move));

        ficha = new ManantialesFicha(4,5, bob.getColor(),
                TokenType.MODERATE_PASTURE);
        move = new ManantialesMove (bob, ficha);
        mv = board.doMove(game, new Move (move));

        ficha = new ManantialesFicha(3,4, charlie.getColor(),
                TokenType.MODERATE_PASTURE);
        move = new ManantialesMove (charlie, ficha);
        mv = board.doMove(game, new Move (move));

        game = board.getGame(gameId);
        ManantialesGame mg = (ManantialesGame) game.getImplementation();
        assertTrue ("CheckConstraint not fired!", mg.getCheckConditions() != null);
        assertEquals (1, mg.getCheckConditions().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSuggestionAccepted () throws InvalidMoveException, JMSException, InvalidSuggestionException {
        Game game = board.getGame(gameId);
        ManantialesGame impl = (ManantialesGame) game.getImplementation();
        impl.setMode (Mode.BASIC_PUZZLE);
        board.shareGame(impl);

        ManantialesFicha play = new ManantialesFicha(5, 4, alice.getColor(), TokenType.MODERATE_PASTURE);
        ManantialesFicha change = new ManantialesFicha(4, 0, alice.getColor(), TokenType.MODERATE_PASTURE);

        ManantialesMove move = new ManantialesMove (alice, play);
        Move mve  = board.doMove (game, new Move(move));
        move = (ManantialesMove) mve.getImplementation();

        System.out.println ("Move: [" + move.getId() + "] =" + move);

        PuzzleSuggestion pzs = new PuzzleSuggestion();
        pzs.setSuggestor(bob);
        pzs.setStatus(SuggestionStatus.UNEVALUATED);
        move = new ManantialesMove (alice,play,change);
        pzs.setMove (move);

        Suggestion suggestion = board.makeSuggestion(game, new Suggestion(pzs));
        pzs = (PuzzleSuggestion) suggestion.getImplementation();
        move = pzs.getMove();

        System.out.println ("Suggestion [" + pzs.getStatus() + "]: " + pzs);
        System.out.println ("Move: [" + move.getId() + "] =" + move);

        assertTrue (pzs.getStatus() == SuggestionStatus.EVALUATED);
        assertTrue ("Move status incorrect!  Status [" + move.getStatus() + "]", move.getStatus().equals(
                MoveStatus.UNVERIFIED));
        System.out.println ("Suggestion [" + pzs.getStatus() + "]: " + pzs);

        pzs.setStatus(SuggestionStatus.ACCEPT);
        suggestion = board.makeSuggestion(game, new Suggestion(pzs));
        pzs = (PuzzleSuggestion) suggestion.getImplementation();
        move = pzs.getMove();

        System.out.println ("Suggestion [" + pzs.getStatus() + "]: " + pzs);
        System.out.println ("Move: [" + move.getId() + "] =" + move);

        assertTrue ("Move not evaluated@!  Status [" + move.getStatus() + "]", move.getStatus().equals(
                MoveStatus.MOVED));

        game = board.getGame(gameId);
        GridGame gridGame = (GridGame) game.getImplementation();
        GameGrid grid = gridGame.getGrid();
        GridCell location =  grid.getLocation(move.getDestinationCell());
        assertTrue ("Destination not populated!", location != null);
        assertTrue ("Destination not populated!", location.equals(change));
        assertTrue ("Location remains!", grid.getLocation(move.getCurrentCell()) == null);
        assertTrue ("Gamegrid is contains both tokens!", grid.getCells().size() == 1);

        System.out.println ("GameGrid: " + grid);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSuggestionRejected () throws InvalidMoveException, JMSException, InvalidSuggestionException {
        Game game = board.getGame(gameId);
        ManantialesGame impl = (ManantialesGame) game.getImplementation();
        impl.setMode (Mode.BASIC_PUZZLE);
        board.shareGame(impl);

        ManantialesFicha play = new ManantialesFicha(5, 4, alice.getColor(), TokenType.MODERATE_PASTURE);
        ManantialesFicha change = new ManantialesFicha(4, 0, alice.getColor(), TokenType.MODERATE_PASTURE);

        ManantialesMove move = new ManantialesMove (alice, play);
        board.doMove (game, new Move(move));

        PuzzleSuggestion pzs = new PuzzleSuggestion();
        pzs.setSuggestor(bob);
        pzs.setStatus(SuggestionStatus.UNEVALUATED);
        move = new ManantialesMove (alice,play,change);
        pzs.setMove (move);

        Suggestion suggestion = board.makeSuggestion(game, new Suggestion(pzs));
        pzs = (PuzzleSuggestion) suggestion.getImplementation();
        move = pzs.getMove();

        assertTrue (pzs.getStatus() == SuggestionStatus.EVALUATED);

        pzs.setStatus(SuggestionStatus.REJECT);
        suggestion = board.makeSuggestion(game, new Suggestion(pzs));
        pzs = (PuzzleSuggestion) suggestion.getImplementation();
        move = pzs.getMove();
        assertTrue ("Move evaluated!  Status [" + move.getStatus() + "]", move.getStatus().equals(MoveStatus.UNVERIFIED));

        game = board.getGame(gameId);
        GridGame gridGame = (GridGame) game.getImplementation();
        GameGrid grid = gridGame.getGrid();
        GridCell location =  grid.getLocation(move.getDestinationCell());
        assertTrue ("Destination populated!", location == null);
        assertTrue ("Location does not remain!", grid.getLocation(move.getCurrentCell()) != null);
        assertTrue ("Gamegrid is contains both tokens!", grid.getCells().size() == 1);
    }
}
