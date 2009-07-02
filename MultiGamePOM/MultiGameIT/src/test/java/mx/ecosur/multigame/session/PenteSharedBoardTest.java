/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.GameType;

import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.ejb.entity.Cell;
import mx.ecosur.multigame.impl.ejb.entity.GameGrid;
import mx.ecosur.multigame.impl.ejb.entity.GamePlayer;
import mx.ecosur.multigame.impl.ejb.entity.Player;
import mx.ecosur.multigame.impl.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.impl.ejb.entity.pente.PenteMove;
import mx.ecosur.multigame.impl.ejb.entity.pente.PentePlayer;
import mx.ecosur.multigame.impl.model.GameState;

import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PenteSharedBoardTest {
	
	private RegistrarRemote registrar;
	
	private SharedBoardRemote board;
	
	private int gameId;
	
	private PentePlayer alice, bob, charlie, denise;
	
	private Cell center;
	

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
		
		gameId = registrar.registerPlayer(registrants [ 0 ], Color.YELLOW, GameType.PENTE).getGame().getId();
		registrar.registerPlayer(registrants [ 1 ], Color.BLUE, GameType.PENTE);
		registrar.registerPlayer(registrants [ 2 ], Color.GREEN, GameType.PENTE);
		registrar.registerPlayer(registrants [ 3 ], Color.RED, GameType.PENTE);
		
		/* Get the SharedBoard */
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.SharedBoardRemote");
	
		
		int row = board.getGame(gameId).getRows()/2;
		int column = board.getGame(gameId).getColumns()/2;
		
		center = new Cell (row, column, Color.YELLOW);
		
		/* Set the GamePlayers from the SharedBoard */
		List<GamePlayer> players = board.getPlayers(gameId);
		for (GamePlayer p : players) {
			for (Player r : registrants) {
				if (p.getPlayer().getName().equals("alice"))
					alice = (PentePlayer) p;
				else if (p.getPlayer().getName().equals("bob"))
					bob = (PentePlayer) p;
				else if (p.getPlayer().getName().equals("charlie"))
					charlie = (PentePlayer) p;
				else if (p.getPlayer().getName().equals("denise"))
					denise = (PentePlayer) p;
			}
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
		assertTrue (board.getGameGrid(gameId).getCells().size() == 0);
	}
	
	/** 
	 * Tests the first move logic.  This tests the positive condition.
	 * @throws InvalidMoveException 
	 * @throws RemoteException 
	 */
	@Test
	public void testFirstMove () throws InvalidMoveException, RemoteException {
		PenteMove move = new PenteMove (alice, center);
		board.move(move);
		
		GameGrid grid = board.getGameGrid(gameId);
		assertNotNull (grid.getLocation(move.getDestination()));
	}	

	/**
	 * Tests the first move logic.  This tests the negative condition.
	 * @throws RemoteException 
	 */
	@Test
	public void testBadFirstMove () throws RemoteException {
		int row = center.getRow() -1;
		int col = center.getColumn() + 1;
		
		Cell center = new Cell (row, col, alice.getColor());
		PenteMove move = new PenteMove (alice, center);

		try {
			board.move (move);
			fail ("Invalid Move must be thrown!");
		} catch (InvalidMoveException e) {
			assertTrue (e != null);
		} 
	}
	
	/* @Test */
	public void testFormTria () throws InvalidMoveException, RemoteException {
		/* Round 1 */
		
		PenteMove move = new PenteMove (alice, center);
		board.move(move);
		
		bob.setTurn(true);
		Cell cell = new Cell (1, 1, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);;
		cell = new Cell (3,1, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5,1, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Round 2 */
		
		alice.setTurn(true);
		cell = new Cell (7, 1, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (1, 2, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (3,2, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5, 2, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		
		/* Round 3 */
		
		alice.setTurn(true);
		cell = new Cell (7, 2, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (1, 3, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		assertEquals (1, move.getTrias().size());
		
		List<GamePlayer> players = board.getPlayers(gameId);
		for (GamePlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (PentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTrias().size());
	}
	
	/* @Test */
	public void testSelfishScoring () throws InvalidMoveException, RemoteException {
		
		/* Round 1 */
		PenteMove move = new PenteMove (alice, center);
		board.move(move);
		
		bob.setTurn(true);
		Cell cell = new Cell (1, 1, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (3,1, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5,1, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Round 2 */
		alice.setTurn(true);
		cell = new Cell (7, 1, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (1, 2, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (3,2, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5, 2, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		
		/* Round 3 */
		alice.setTurn(true);
		cell = new Cell (7, 2, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (1, 3, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		assertEquals (1, move.getTrias().size());
		
		List<GamePlayer> players = board.getPlayers(gameId);
		for (GamePlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (PentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTrias().size());
		bob.setTurn(true);
		
		charlie.setTurn(true);
		cell = new Cell (3,3, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5, 3, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Round 4 */
		alice.setTurn(true);
		cell = new Cell (7, 3, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (9,1, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (3, 5, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5, 5, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Round 5 */
		alice.setTurn(true);
		cell = new Cell (7, 5, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (9, 2, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (3, 6, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5, 6, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Round 6 */
		alice.setTurn(true);
		cell = new Cell (7, 6, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (9,3, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		/* Game should be over, with Bob the winner */
		PenteGame pente = (PenteGame) board.getGame(gameId);
		assertEquals (GameState.END, pente.getState()); 
		
		Set <PentePlayer> winners = pente.getWinners();
		
		assertTrue (winners.size() == 1);
		for (PentePlayer player: winners) {
			assertEquals (bob.getId(), player.getId());
			assertEquals (player.getPoints(), 10);
		}
	}
	
	/* @Test */
	public void testCooperativeScoring () throws InvalidMoveException, RemoteException {
		/* Round 1 */
		PenteMove move = new PenteMove (alice, center);
		board.move(move);
		
		bob.setTurn(true);
		Cell cell = new Cell (1, 1, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (3,1, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5,1, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Round 2 */
		alice.setTurn(true);
		cell = new Cell (7, 1, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (1, 2, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (3,2, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5, 2, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		
		/* Round 3 */
		alice.setTurn(true);
		cell = new Cell (7, 2, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (3, 3, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (1, 3, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5, 3, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Assert that Denise has completed a tria */
		assertEquals (1, move.getTrias().size());
		
		List<GamePlayer> players = board.getPlayers(gameId);
		for (GamePlayer player : players) {
			if (! (player.getId() == denise.getId()))
				continue;
			denise = (PentePlayer) player;
			break;
		}
		
		assertEquals (1, denise.getTrias().size());
		denise.setTurn(true);
		
		/* Round 4 */
		alice.setTurn(true);
		cell = new Cell (7, 3, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (1,4, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		/* Assert that Bob has completed a tessera */
		assertEquals (1, move.getTesseras().size());
		
		players = board.getPlayers(gameId);
		for (GamePlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (PentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTesseras().size());
		bob.setTurn(true);
		
		charlie.setTurn(true);
		cell = new Cell (3, 4, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		/* Assert that Charlie has completed a tessera */
		assertEquals (1, move.getTesseras().size());
		
		denise.setTurn(true);
		cell = new Cell (5, 5, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Round 5 */
		alice.setTurn(true);
		cell = new Cell (7, 5, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (0, 3, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		charlie.setTurn(true);
		cell = new Cell (2, 5, charlie.getColor());
		move = new PenteMove (charlie, cell);
		board.move(move);
		
		denise.setTurn(true);
		cell = new Cell (5, 7, denise.getColor());
		move = new PenteMove (denise, cell);
		board.move(move);
		
		/* Round 6 */
		alice.setTurn(true);
		cell = new Cell (7, 6, alice.getColor());
		move = new PenteMove (alice, cell);
		board.move(move);
		
		bob.setTurn(true);
		cell = new Cell (3, 6, bob.getColor());
		move = new PenteMove (bob, cell);
		board.move(move);
		
		/* Confirm cooperative scoring */
		
		players = board.getPlayers(gameId);
		for (GamePlayer player : players) {
			if (player.getId() == charlie.getId())
				charlie = (PentePlayer) player;
			if (player.getId() == bob.getId())
				bob = (PentePlayer) player;
		}
		
		/* Charlie should have been assigned the new tessera (as well as his
		 * partner bob's previous one */
		assertEquals (3, charlie.getTesseras().size());
		assertEquals (3, bob.getTesseras().size());
		
		/* Check the scoring function */
		assertEquals (5, charlie.getPoints());
		assertEquals (5, bob.getPoints());

		/* Check the "find-the-winners" rule */
		PenteGame pente = (PenteGame) board.getGame(gameId);
		assertEquals (GameState.END, pente.getState()); 
		
		Set <PentePlayer> winners = pente.getWinners();
		assertEquals (2, winners.size());
		for (PentePlayer player: winners) {
			assertEquals (player.getPoints(), 5);
			assertTrue (bob.getId() == player.getId() || charlie.getId() == player.getId());
		}
	}
}
