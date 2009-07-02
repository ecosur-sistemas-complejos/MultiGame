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

import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.ejb.entity.GamePlayer;
import mx.ecosur.multigame.impl.ejb.entity.Player;
import mx.ecosur.multigame.manantiales.TokenType;
import mx.ecosur.multigame.model.manantiales.Ficha;
import mx.ecosur.multigame.model.manantiales.ManantialesGame;
import mx.ecosur.multigame.model.manantiales.ManantialesMove;
import mx.ecosur.multigame.model.manantiales.ManantialesPlayer;

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
			"mx.ecosur.multigame.ejb.RegistrarRemote");
		
		Player[] registrants = {
			new Player ("alice"),
			new Player ("bob"),
			new Player ("charlie"),
			new Player ("denise")};
		
		gameId = registrar.registerPlayer(registrants [ 0 ], Color.YELLOW, GameType.MANANTIALES).getGame().getId();
		registrar.registerPlayer(registrants [ 1 ], Color.BLUE, GameType.MANANTIALES);
		registrar.registerPlayer(registrants [ 2 ], Color.GREEN, GameType.MANANTIALES);
		registrar.registerPlayer(registrants [ 3 ], Color.RED, GameType.MANANTIALES);
		
		/* Get the SharedBoard */
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.SharedBoardRemote");

		/* Set the GamePlayers from the SharedBoard */
		List<GamePlayer> players = board.getPlayers(gameId);
		for (GamePlayer p : players) {
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
		registrar.unregisterPlayer(alice);
		registrar.unregisterPlayer(bob);
		registrar.unregisterPlayer(charlie);
		registrar.unregisterPlayer(denise);
	}	
	
	
	/**
	 * Simple test to determine if there are the correct number of squares
	 * after the game state is set to BEGIN.
	 * @throws RemoteException
	 */
	@Test
	public void testGetGameGrid() throws RemoteException {
		ManantialesGame game = (ManantialesGame) board.getGame(gameId);
		assertTrue (game.getGrid().getCells().size() == 0);
	}	
	
	/** Test on ManantialesGame for setting check constraints 
	 * @throws InvalidMoveException */
	@Test
	public void testCheckConstraints () throws InvalidMoveException {
		ManantialesGame game = (ManantialesGame) board.getGame(gameId);
		Ficha ficha = new Ficha (4,3, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		
		ManantialesMove move = new ManantialesMove (alice, ficha);
		move = (ManantialesMove) board.move(move);
		game = (ManantialesGame) move.getPlayer().getGame();
		
		ficha = new Ficha (4,5, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		move = new ManantialesMove (bob, ficha);
		move = (ManantialesMove) board.move(move);
		game = (ManantialesGame) move.getPlayer().getGame();
		
		ficha = new Ficha (3,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
		move = new ManantialesMove (charlie, ficha);
		move = (ManantialesMove) board.move (move);
		game = (ManantialesGame) move.getPlayer().getGame();
		
		assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
		assertEquals (1, game.getCheckConditions().size());
		
		List<GamePlayer> players = board.getPlayers(gameId);
		for (GamePlayer player : players) {
			ManantialesGame playerGame = (ManantialesGame) player.getGame();
			assertTrue ("Registrant has no check constraints, while the game does.", 
					playerGame.getCheckConditions() != null);
			assertTrue (playerGame.getCheckConditions().size() == game.getCheckConditions().size());
		}
	}
}
