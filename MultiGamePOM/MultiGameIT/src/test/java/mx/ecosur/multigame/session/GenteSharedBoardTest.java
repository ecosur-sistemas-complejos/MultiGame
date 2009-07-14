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

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;
import mx.ecosur.multigame.impl.entity.gente.GentePlayer;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Move;
import mx.ecosur.multigame.model.Registrant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GenteSharedBoardTest {
	
	private RegistrarRemote registrar;
	
	private SharedBoardRemote board;
	
	private int gameId;
	
	private GentePlayer alice, bob, charlie, denise;
	
	private GridCell center;
	

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
		
		GenteGame game = new GenteGame ();	
		Game boardGame = new Game (game);
		for (int i = 0; i < 4; i++) { 
			GamePlayer player = registrar.registerAgent(boardGame, 
					new Registrant (registrants [ i ]));
			if (gameId == 0) {
				GridPlayer gp = (GridPlayer) player.getImplementation();
				gameId = gp.getGame().getId();
				boardGame = new Game(gp.getGame());
			}
		}
		
		/* Get the SharedBoard */
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote");	
		boardGame = board.getGame(gameId);
		game = (GenteGame) boardGame.getImplementation();
		
		int row = game.getRows()/2;
		int column = game.getColumns()/2;
		
		center = new GridCell (row, column, Color.YELLOW);
		
		/* Set the GamePlayers from the SharedBoard */
		List<GridPlayer> players = game.getPlayers();
		for (GridPlayer p : players) {
			if (p.getRegistrant().getName().equals("alice"))
				alice = (GentePlayer) p;
			else if (p.getRegistrant().getName().equals("bob"))
				bob = (GentePlayer) p;
			else if (p.getRegistrant().getName().equals("charlie"))
				charlie = (GentePlayer) p;
			else if (p.getRegistrant().getName().equals("denise"))
				denise = (GentePlayer) p;
		}
	}
	
	@After
	public void tearDown () throws NamingException, RemoteException, InvalidRegistrationException {
		Game boardGame = board.getGame(gameId);		
		registrar.unregisterPlayer(boardGame, new GamePlayer (alice));
		registrar.unregisterPlayer(boardGame, new GamePlayer (bob));
		registrar.unregisterPlayer(boardGame, new GamePlayer (charlie));
		registrar.unregisterPlayer(boardGame, new GamePlayer (denise));
	}
	
	/**
	 * Simple test to determine if there are the correct number of squares
	 * after the game state is set to BEGIN.
	 * @throws RemoteException
	 */
	@Test
	public void testGetGameGrid() throws RemoteException {
		Game boardGame = board.getGame(gameId);
		GenteGame game = (GenteGame) boardGame.getImplementation();
		assertTrue (game.getGrid().getCells().size() == 0);
	}
	
	/** 
	 * Tests the first move logic.  This tests the positive condition.
	 * @throws InvalidMoveException 
	 * @throws RemoteException 
	 */
	@Test
	public void testFirstMove () throws InvalidMoveException, RemoteException {
		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
		GenteMove move = new GenteMove (alice, center);
		Move mv = board.move(new Game (game), new Move (move));
		move = (GenteMove) mv.getImplementation();
		game = (GenteGame) board.getGame(gameId).getImplementation();
		assertNotNull (game.getGrid().getLocation(move.getDestination()));
	}	

	/**
	 * Tests the first move logic.  This tests the negative condition.
	 * @throws RemoteException 
	 */
	@Test
	public void testBadFirstMove () throws RemoteException {
		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
		int row = center.getRow() -1;
		int col = center.getColumn() + 1;
		
		GridCell center = new GridCell (row, col, alice.getColor());
		GenteMove move = new GenteMove (alice, center);

		try {
			board.move (new Game (game), new Move (move));
			fail ("Invalid Move must be thrown!");
		} catch (InvalidMoveException e) {
			assertTrue (e != null);
		} 
	}
	
	@Test
	public void testFormTria () throws InvalidMoveException, RemoteException {
		/* Round 1 */
		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
		GenteMove move = new GenteMove (alice, center);
		Move mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		GridCell cell = new GridCell (1, 1, bob.getColor());
		move = new GenteMove (bob, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		cell = new GridCell (3,1, charlie.getColor());
		move = new GenteMove (charlie, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		cell = new GridCell (5,1, denise.getColor());
		move = new GenteMove (denise, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		/* Round 2 */
		
		cell = new GridCell (7, 1, alice.getColor());
		move = new GenteMove (alice, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		cell = new GridCell (1, 2, bob.getColor());
		move = new GenteMove (bob, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		cell = new GridCell (3,2, charlie.getColor());
		move = new GenteMove (charlie, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		cell = new GridCell (5, 2, denise.getColor());
		move = new GenteMove (denise, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		
		/* Round 3 */

		cell = new GridCell (7, 2, alice.getColor());
		move = new GenteMove (alice, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		cell = new GridCell (1, 3, bob.getColor());
		move = new GenteMove (bob, cell);
		mv = board.move(new Game (game), new Move (move));
		game = (GenteGame) mv.getPlayer().getGame().getImplementation();
		
		assertEquals (1, move.getTrias().size());
		
		game = (GenteGame) board.getGame(gameId).getImplementation();
		List<GridPlayer> players = game.getPlayers();
		for (GridPlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (GentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTrias().size());
	}
	
	@Test
	public void testSelfishScoring () throws InvalidMoveException, RemoteException {
		
		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
		
		/* Round 1 */
		GenteMove move = new GenteMove (alice, center);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		GridCell cell = new GridCell (1, 1, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		charlie.setTurn(true);
		cell = new GridCell (3,1, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5,1, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		/* Round 2 */
		alice.setTurn(true);
		cell = new GridCell (7, 1, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (1, 2, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		charlie.setTurn(true);
		cell = new GridCell (3,2, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5, 2, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		
		/* Round 3 */
		alice.setTurn(true);
		cell = new GridCell (7, 2, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (1, 3, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		assertEquals (1, move.getTrias().size());
		
		game = (GenteGame) board.getGame(gameId).getImplementation();
		List<GridPlayer> players = game.getPlayers();
		for (GridPlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (GentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTrias().size());
		bob.setTurn(true);
		
		charlie.setTurn(true);
		cell = new GridCell (3,3, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5, 3, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		/* Round 4 */
		alice.setTurn(true);
		cell = new GridCell (7, 3, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (9,1, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		charlie.setTurn(true);
		cell = new GridCell (3, 5, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5, 5, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		/* Round 5 */
		alice.setTurn(true);
		cell = new GridCell (7, 5, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (9, 2, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		charlie.setTurn(true);
		cell = new GridCell (3, 6, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5, 6, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		/* Round 6 */
		alice.setTurn(true);
		cell = new GridCell (7, 6, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (9,3, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		/* Game should be over, with Bob the winner */
		game = (GenteGame) board.getGame(gameId).getImplementation();
		assertEquals (GameState.END, game.getState()); 
		
		Set <GentePlayer> winners = game.getWinners();
		
		assertTrue (winners.size() == 1);
		for (GentePlayer player: winners) {
			assertEquals (bob.getId(), player.getId());
			assertEquals (player.getPoints(), 10);
		}
	}
	
	@Test
	public void testCooperativeScoring () throws InvalidMoveException, RemoteException {
		GenteGame game = (GenteGame) board.getGame(gameId).getImplementation();
		
		/* Round 1 */
		GenteMove move = new GenteMove (alice, center);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		GridCell cell = new GridCell (1, 1, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		charlie.setTurn(true);
		cell = new GridCell (3,1, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5,1, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		/* Round 2 */
		alice.setTurn(true);
		cell = new GridCell (7, 1, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (1, 2, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		charlie.setTurn(true);
		cell = new GridCell (3,2, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5, 2, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		
		/* Round 3 */
		alice.setTurn(true);
		cell = new GridCell (7, 2, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (3, 3, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		charlie.setTurn(true);
		cell = new GridCell (1, 3, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5, 3, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		/* Assert that Denise has completed a tria */
		assertEquals (1, move.getTrias().size());
		
		game = (GenteGame) board.getGame(gameId).getImplementation();
		List<GridPlayer> players = game.getPlayers();
		for (GridPlayer player : players) {
			if (! (player.getId() == denise.getId()))
				continue;
			denise = (GentePlayer) player;
			break;
		}
		
		assertEquals (1, denise.getTrias().size());
		denise.setTurn(true);
		
		/* Round 4 */
		alice.setTurn(true);
		cell = new GridCell (7, 3, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (1,4, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		/* Assert that Bob has completed a tessera */
		assertEquals (1, move.getTesseras().size());
		
		game = (GenteGame) board.getGame(gameId).getImplementation();
		players = game.getPlayers();
		for (GridPlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (GentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTesseras().size());
		bob.setTurn(true);
		
		charlie.setTurn(true);
		cell = new GridCell (3, 4, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		/* Assert that Charlie has completed a tessera */
		assertEquals (1, move.getTesseras().size());
		
		denise.setTurn(true);
		cell = new GridCell (5, 5, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		/* Round 5 */
		alice.setTurn(true);
		cell = new GridCell (7, 5, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (0, 3, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		charlie.setTurn(true);
		cell = new GridCell (2, 5, charlie.getColor());
		move = new GenteMove (charlie, cell);
		board.move(new Game (game), new Move (move));
		
		denise.setTurn(true);
		cell = new GridCell (5, 7, denise.getColor());
		move = new GenteMove (denise, cell);
		board.move(new Game (game), new Move (move));
		
		/* Round 6 */
		alice.setTurn(true);
		cell = new GridCell (7, 6, alice.getColor());
		move = new GenteMove (alice, cell);
		board.move(new Game (game), new Move (move));
		
		bob.setTurn(true);
		cell = new GridCell (3, 6, bob.getColor());
		move = new GenteMove (bob, cell);
		board.move(new Game (game), new Move (move));
		
		/* Confirm cooperative scoring */
		
		game = (GenteGame) board.getGame(gameId).getImplementation();
		players = game.getPlayers();
		for (GridPlayer player : players) {
			if (player.getId() == charlie.getId())
				charlie = (GentePlayer) player;
			if (player.getId() == bob.getId())
				bob = (GentePlayer) player;
		}
		
		/* Charlie should have been assigned the new tessera (as well as his
		 * partner bob's previous one */
		assertEquals (3, charlie.getTesseras().size());
		assertEquals (3, bob.getTesseras().size());
		
		/* Check the scoring function */
		assertEquals (5, charlie.getPoints());
		assertEquals (5, bob.getPoints());

		/* Check the "find-the-winners" rule */
		GenteGame pente = (GenteGame) board.getGame(gameId).getImplementation();
		assertEquals (GameState.END, pente.getState()); 
		
		Set <GentePlayer> winners = pente.getWinners();
		assertEquals (2, winners.size());
		for (GentePlayer player: winners) {
			assertEquals (player.getPoints(), 5);
			assertTrue (bob.getId() == player.getId() || charlie.getId() == player.getId());
		}
	}
}
