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

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.checkers.Checker;

import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.checkers.JumpMove;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CheckersSharedBoardTest {

	private RegistrarRemote registrar;

	private SharedBoardRemote board;

	private GameGrid jumpGrid;

	private GamePlayer alice;

	private GamePlayer bob;

	private int gameId;

	private Checker[] jumps = { new Checker(1, 1, Color.RED),
			new Checker(3, 1, Color.RED), new Checker(5, 1, Color.RED) };

	@Before
	public void fixtures() throws RemoteException, NamingException,
			InvalidRegistrationException {
		InitialContext ic = new InitialContext();

		registrar = (RegistrarRemote) ic
				.lookup("mx.ecosur.multigame.ejb.RegistrarRemote");

		Player a = registrar.locatePlayer("alice");
		Player b = registrar.locatePlayer("bob");

		alice = registrar.registerPlayer(a, Color.YELLOW, GameType.CHECKERS);
		bob = registrar.registerPlayer(b, Color.RED, GameType.CHECKERS);

		/* save the game id */
		gameId = alice.getGame().getId();

		/* Initalize the jumpGrid */
		jumpGrid = new GameGrid();
		jumpGrid.updateCell(new Checker(0, 0, Color.YELLOW));
		for (int i = 0; i < jumps.length; i++) {
			jumpGrid.updateCell(jumps[i]);
		}

		/* Get the SharedBoard */
		board = (SharedBoardRemote) ic
				.lookup("mx.ecosur.multigame.ejb.SharedBoardRemote");
	}

	@After
	public void tearDown() throws NamingException, RemoteException,
			InvalidRegistrationException {
		registrar.unregisterPlayer(alice);
		registrar.unregisterPlayer(bob);
	}

	/**
	 * Simple test to determine if there are the correct number of squares after
	 * the game state is set to BEGIN.
	 * 
	 * @throws RemoteException
	 */
	@Test
	public void testGetGameGrid() throws RemoteException {
		GameGrid grid = board.getGameGrid(gameId);
		Set<Cell> cells = grid.getCells();
		assertEquals(24, cells.size());
	}

	/**
	 * Simple test for move validation, tests if a piece can be moved
	 * diagonally, for one space.
	 * 
	 * @throws RemoteException
	 * @throws InvalidMoveException
	 */
	@Test
	public void testValidateMove() throws RemoteException, InvalidMoveException {
		alice.setTurn(true);

		/* Move a piece diagonally and validate */
		Checker start = new Checker(2, 0, alice.getColor());
		Checker destination = new Checker(3, 1, alice.getColor());
		Move move = new Move(alice, start, destination);
		move = board.validateMove(move);
		assertTrue(move.getStatus() == Move.Status.VERIFIED);
	}

	/**
	 * Simple test for move validation. Tests an invalid, e.g. horizontal, move
	 * on the board.
	 * 
	 * @throws RemoteException
	 */
	@Test
	public void testInvalidateMove() throws RemoteException {
		alice.setTurn(true);

		/* Move a piece horizontally and validate */
		Checker start = new Checker(2, 0, alice.getColor());
		Checker destination = new Checker(3, 0, alice.getColor());
		Move move = new Move(alice, start, destination);

		try {
			board.validateMove(move);
			fail("Exception must be thrown!");
		} catch (InvalidMoveException e) {
		}
	}

	/**
	 * Simple test for verifying that a move with an invalid starting position
	 * will not be validated.
	 * 
	 * @throws RemoteException
	 */
	@Test
	public void testInvalidateMoveWithInvalidStart() throws RemoteException {
		alice.setTurn(true);

		/* Move a piece horizontally and validate */
		Checker start = new Checker(3, 1, alice.getColor());
		Checker destination = new Checker(4, 2, alice.getColor());

		Move move = new Move(alice, start, destination);
		try {
			board.validateMove(move);
			fail("Exception must be thrown!");
		} catch (InvalidMoveException e) {
		}
	}

	/**
	 * Simple test to ensure that a token may not be moved on top of another
	 * token (i.e. an invalid destination).
	 * 
	 * @throws RemoteException
	 */
	@Test
	public void testInvalidateMoveWithInvalidDestination()
			throws RemoteException {
		alice.setTurn(true);

		/* Move a piece horizontally and validate */
		Checker start = new Checker(0, 0, alice.getColor());
		Checker destination = new Checker(1, 1, alice.getColor());

		Move move = new Move(alice, start, destination);
		assertTrue(move.getStatus() == Move.Status.UNVERIFIED);
		try {
			board.validateMove(move);
			fail("Exception must be thrown!");
		} catch (InvalidMoveException e) {
		}
	}

	/**
	 * Simple test for incrementing turns between two players of checkers.
	 * 
	 * @throws RemoteException
	 * @throws RemoteException
	 */
	@Test
	public void testIncrementTurn() throws RemoteException {
		alice.setTurn(true);
		bob.setTurn(false);
		GamePlayer next = board.incrementTurn(alice);
		assertNotNull(next);
		assertTrue(next.isTurn());
	}

	/**
	 * Test that a valid move can be moved on the GameGrid.
	 * 
	 * @throws RemoteException
	 * @throws InvalidMoveException
	 */

	@Test
	public void testValidMove() throws RemoteException, InvalidMoveException {
		alice.setTurn(true);

		/* Move a piece diagonally and validate */
		Checker start = new Checker(2, 0, alice.getColor());
		Checker destination = new Checker(3, 1, alice.getColor());
		Move move = new Move(alice, start, destination);
		move = board.validateMove(move);
		board.move(move);

		assertTrue(board.getGameGrid(gameId).getLocation(move.getDestination()) != null);
		assertTrue(board.getGameGrid(gameId).getLocation(move.getCurrent()) == null);
	
	}

	/**
	 * Tests that
	 * 
	 * @throws RemoteException
	 */
	@Test
	public void testInvalidMove() throws RemoteException {
		alice.setTurn(true);

		/* Move a piece horizontally and validate */
		Checker start = new Checker(2, 2, alice.getColor());
		Checker destination = new Checker(3, 2, alice.getColor());
		Move move = new Move(alice, start, destination);

		try {
			move = board.validateMove(move);
			fail("Exception must be thrown!");
		} catch (InvalidMoveException e) {
		}

		try {
			board.move(move);
			fail("Exception must be thrown!");
		} catch (InvalidMoveException e) {
		}

		GameGrid grid = board.getGameGrid(gameId);
		assertTrue(grid.getLocation(move.getDestination()) == null);
		assertTrue(grid.getLocation(move.getCurrent()) != null);
	}

	public void testJumpMoveValidation() throws RemoteException {
		alice.setTurn(true);

		Checker start = new Checker(0, 0, alice.getColor());
		Checker stop = new Checker(6, 2, alice.getColor());

		JumpMove jump = new JumpMove(alice, start, stop);
		List<Move> moves = composeJumpSequence(jumps);
		for (Move m : moves) {
			jump.addJump(m);
		}
	}

	private List<Move> composeJumpSequence(Checker[] jumps2) {
		// TODO Auto-generated method stub
		return null;
	}
}
