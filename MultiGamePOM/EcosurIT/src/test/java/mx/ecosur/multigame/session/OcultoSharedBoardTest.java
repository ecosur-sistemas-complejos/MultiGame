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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;

import mx.ecosur.multigame.impl.entity.oculto.Ficha;
import mx.ecosur.multigame.impl.entity.oculto.OcultoGame;
import mx.ecosur.multigame.impl.entity.oculto.OcultoMove;
import mx.ecosur.multigame.impl.entity.oculto.OcultoPlayer;
import mx.ecosur.multigame.impl.enums.oculto.TokenType
        ;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Move;
import mx.ecosur.multigame.model.Registrant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OcultoSharedBoardTest {


	private RegistrarRemote registrar;

	private SharedBoardRemote board;

	private int gameId;

	private OcultoPlayer alice, bob, charlie, denise;


	@Before
	public void fixtures () throws RemoteException, NamingException, InvalidRegistrationException {
		InitialContext ic = new InitialContext();

		registrar = (RegistrarRemote) ic.lookup(
			"mx.ecosur.multigame.ejb.interfaces.RegistrarRemote");

		GridRegistrant[] registrants = {
			new GridRegistrant ("alice"),
			new GridRegistrant ("bob"),
			new GridRegistrant ("charlie"),
			new GridRegistrant ("denise")};

		OcultoGame game = new OcultoGame ();
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
		game = (OcultoGame) board.getGame(gameId).getImplementation();

		/* Set the GamePlayers from the SharedBoard */
		List<GridPlayer> players = game.getPlayers();
		for (GridPlayer p : players) {
			if (p.getRegistrant().getName().equals("alice"))
				alice = (OcultoPlayer) p;
			else if (p.getRegistrant().getName().equals("bob"))
				bob = (OcultoPlayer) p;
			else if (p.getRegistrant().getName().equals("charlie"))
				charlie = (OcultoPlayer) p;
			else if (p.getRegistrant().getName().equals("denise"))
				denise = (OcultoPlayer) p;
		}

		assertNotNull ("Alice not found in game!", alice);
		assertNotNull ("Bob not found in game!", bob);
		assertNotNull ("Charlie not found in game!", charlie);
		assertNotNull ("Denise not found in game!", denise);
	}

	@After
	public void tearDown () throws NamingException, RemoteException, InvalidRegistrationException {
		OcultoGame game = (OcultoGame) board.getGame(gameId).getImplementation();
		registrar.unregister(new Game (game), new GamePlayer (alice));
		registrar.unregister(new Game (game), new GamePlayer (bob));
		registrar.unregister(new Game (game), new GamePlayer (charlie));
		registrar.unregister(new Game (game), new GamePlayer (denise));
	}


	/**
	 * Simple test to determine if there are the correct number of squares
	 * after the game state is set to BEGIN.
	 * @throws java.rmi.RemoteException
	 */
	@Test
	public void testGetGameGrid() throws RemoteException {
		OcultoGame game = (OcultoGame) board.getGame(gameId).getImplementation();
		assertTrue (game.getGrid().getCells().size() == 0);
	}

	/** Test on OcultoGame for setting check constraints
	 * @throws mx.ecosur.multigame.exception.InvalidMoveException */
	@Test
	public void testCheckConstraints () throws InvalidMoveException {
		Game game = board.getGame(gameId);
		Ficha ficha = new Ficha (4,3, alice.getColor(),
				TokenType.MODERATE_PASTURE);

		OcultoMove move = new OcultoMove (alice, ficha);
		Move mv = board.doMove(game, new Move (move));

		ficha = new Ficha (4,5, bob.getColor(),
				TokenType.MODERATE_PASTURE);
		move = new OcultoMove (bob, ficha);
		mv = board.doMove(game, new Move (move));

		ficha = new Ficha (3,4, charlie.getColor(),
				TokenType.MODERATE_PASTURE);
		move = new OcultoMove (charlie, ficha);
		mv = board.doMove(game, new Move (move));

        game = board.getGame(gameId);
		OcultoGame mg = (OcultoGame) game.getImplementation();
		assertTrue ("CheckConstraint not fired!", mg.getCheckConditions() != null);
		assertEquals (1, mg.getCheckConditions().size());
	}
}