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
import mx.ecosur.multigame.impl.entity.manantiales.Ficha;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesPlayer;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Move;
import mx.ecosur.multigame.model.Registrant;

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
		InitialContext ic = new InitialContext();
		
		registrar = (RegistrarRemote) ic.lookup(
			"mx.ecosur.multigame.ejb.interfaces.RegistrarRemote");
		
		GridRegistrant[] registrants = {
			new GridRegistrant ("alice"),
			new GridRegistrant ("bob"),
			new GridRegistrant ("charlie"),
			new GridRegistrant ("denise")};
		
		ManantialesGame game = new ManantialesGame ();
		
		for (int i = 0; i < registrants.length; i++) {
			GamePlayer player = registrar.registerAgent(new Game (game), 
					new Registrant (registrants [ i ]));
			if (gameId == 0) {
				GridPlayer gp = (GridPlayer) player.getImplementation();
				gameId = gp.getGame().getId();
			}
		}
		
		/* Get the SharedBoard */
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote");
		game = (ManantialesGame) board.getGame(gameId).getImplementation();

		/* Set the GamePlayers from the SharedBoard */
		List<GridPlayer> players = game.getPlayers();
		for (GridPlayer p : players) {
			if (p.getPlayer().getName().equals("alice"))
				alice = (ManantialesPlayer) p;
			else if (p.getPlayer().getName().equals("bob"))
				bob = (ManantialesPlayer) p;
			else if (p.getPlayer().getName().equals("charlie"))
				charlie = (ManantialesPlayer) p;
			else if (p.getPlayer().getName().equals("denise"))
				denise = (ManantialesPlayer) p;
		}
	}
	
	@After
	public void tearDown () throws NamingException, RemoteException, InvalidRegistrationException {
		ManantialesGame game = (ManantialesGame) board.getGame(gameId).getImplementation();
		registrar.unregisterPlayer(new Game (game), new GamePlayer (alice));
		registrar.unregisterPlayer(new Game (game), new GamePlayer (bob));
		registrar.unregisterPlayer(new Game (game), new GamePlayer (charlie));
		registrar.unregisterPlayer(new Game (game), new GamePlayer (denise));
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
		ManantialesGame game = (ManantialesGame) board.getGame(gameId).getImplementation();
		Ficha ficha = new Ficha (4,3, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		
		ManantialesMove move = new ManantialesMove (alice, ficha);
		board.move(new Game (game), new Move (move));
		
		ficha = new Ficha (4,5, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		move = new ManantialesMove (bob, ficha);
		board.move(new Game (game), new Move (move));
		
		ficha = new Ficha (3,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
		move = new ManantialesMove (charlie, ficha);
		board.move(new Game (game), new Move (move));
		
		assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
		assertEquals (1, game.getCheckConditions().size());
		
		game = (ManantialesGame) board.getGame(gameId).getImplementation();
		List<GridPlayer> players = game.getPlayers();
		for (GridPlayer player : players) {
			ManantialesGame playerGame = (ManantialesGame) player.getGame();
			assertTrue ("Registrant has no check constraints, while the game does.", 
					playerGame.getCheckConditions() != null);
			assertTrue (playerGame.getCheckConditions().size() == game.getCheckConditions().size());
		}
	}
}
