package mx.edu.multigame.session;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.checkers.Checker;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.checkers.JumpMove;

import org.junit.Before;
import org.junit.Test;

public class CheckersSharedBoardTest {
	
	private SharedBoardRemote board;
	
	private GameGrid jumpGrid;
	
	private Player alice;
	
	private Player bob;
	
	private int id = 0;
	
	private Checker [] jumps = { 
			new Checker (1,1,Color.RED),
			new Checker (3,1,Color.RED),
			new Checker (5,1,Color.RED)
	};

	@Before
	public void fixtures () throws RemoteException, NamingException, InvalidRegistrationException {
		InitialContext ic = new InitialContext();
		
		RegistrarRemote registrar = (RegistrarRemote) ic.lookup(
			"mx.ecosur.multigame.ejb.RegistrarRemote");
		alice = registrar.locatePlayer("alice");
		bob = registrar.locatePlayer("bob");
		
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.SharedBoardRemote");
		board.locateSharedBoard(GameType.CHECKERS);
		
		List<Player> players = board.getPlayers();
		for (Player p : players) {
			registrar.unregisterPlayer(p, GameType.CHECKERS);
		}
		
		registrar.registerPlayer(alice, GameType.CHECKERS);
		registrar.registerPlayer(bob, GameType.CHECKERS);
		
		/* Initalize the jumpGrid */
		jumpGrid = new GameGrid();
		jumpGrid.updateCell (new Checker (0,0,Color.BLACK));
		for (int i = 0; i < jumps.length; i++) {
			jumpGrid.updateCell(jumps [ i ]);
		}
	}
	
	/**
	 * Simple test to determine if there are the correct number of squares
	 * after the game state is set to BEGIN.
	 * @throws RemoteException
	 */
	@Test
	public void testGetGameGrid() throws RemoteException {
		assertTrue (board.getGameGrid().getCells().size() == 24);
	}

	/**
	 * Simple test for move validation, tests if a piece can be
	 * moved diagonally, for one space. 
	 * @throws RemoteException 
	 * @throws InvalidMoveException 
	 */
	@Test
	public void testValidateMove() throws RemoteException,
		InvalidMoveException 
	{
		alice.setTurn(true);

		/* Move a piece diagonally and validate */
		Checker start = new Checker(2,0, alice.getColor());
		Checker destination = new Checker(3,1, alice.getColor());
		Move move = new Move(board.getGame(), alice, start, destination);
		move = board.validateMove(move);
		assertTrue(move.getStatus() == Move.Status.VERIFIED);
	}
	
	/**
	 * Simple test for move validation.  Tests an invalid, e.g. horizontal,
	 * move on the board.
	 * @throws RemoteException 
	 */
	@Test
	public void testInvalidateMove () throws RemoteException {
		alice.setTurn(true);
		
		/* Move a piece horizontally and validate */
		Checker start = new Checker (2,0, alice.getColor());
		Checker destination = new Checker (3,0, alice.getColor());
		Move move = new Move (board.getGame(), alice, start,destination);
		
		try {
			board.validateMove(move);
			fail ("Exception must be thrown!");
		} catch (InvalidMoveException e) {}
	}
	
	/**
	 * Simple test for verifying that a move with an invalid starting
	 * position will not be validated.
	 */
	@Test
	public void testInvalidateMoveWithInvalidStart () {
		alice.setTurn(true);
		
		/* Move a piece horizontally and validate */
		Checker start = new Checker (3,1, alice.getColor());
		Checker destination = new Checker (4,2, alice.getColor());
		
		Move move = new Move (board.getGame(), alice, start,destination);
		try {
			move = board.validateMove(move);
			fail ("Exception must be thrown!");
		} catch (InvalidMoveException e) {}
		
		assertTrue (move.getStatus() == Move.Status.INVALID);
	}
	
	/**
	 * Simple test to ensure that a token may not be moved on top
	 * of another token (i.e. an invalid destination).
	 */
	@Test
	public void testInvalidateMoveWithInvalidDestination () {
		alice.setTurn(true);
		
		/* Move a piece horizontally and validate */
		Checker start = new Checker (0,0, alice.getColor());
		Checker destination = new Checker (1,1, alice.getColor());
		
		Move move = new Move (board.getGame(), alice, start, destination);
		assertTrue (move.getStatus() == Move.Status.UNVERIFIED);
		try {
			board.validateMove(move);
			fail ("Exception must be thrown!");
		} catch (InvalidMoveException e) {}	
	}
	
	/**
	 * Simple test for incrementing turns between two players of
	 * checkers.
	 * @throws RemoteException
	 */
	@Test
	public void testIncrementTurn() throws RemoteException {
		alice.setTurn(true);
		bob.setTurn (false);
		
		board.incrementTurn(alice);
		
		assertTrue (bob.isTurn());
		assertFalse (alice.isTurn());
		
		board.incrementTurn(bob);
		
		assertTrue (alice.isTurn());
		assertFalse (bob.isTurn());
	}
	
	/**
	 * Test that a valid move can be moved on the GameGrid.
	 * @throws RemoteException
	 * @throws InvalidMoveException
	 */
	
	@Test
	public void testValidMove () throws RemoteException, InvalidMoveException {
		alice.setTurn(true);

		/* Move a piece diagonally and validate */
		Checker start = new Checker(2, 0, alice.getColor());
		Checker destination = new Checker(3, 1, alice.getColor());
		Move move = new Move(board.getGame(), alice, start, destination);
		move = board.validateMove(move);
		board.move(move);
		
		assertTrue (move.getStatus() == Move.Status.MOVED);
		assertTrue (board.getGame().getGrid().getLocation(move.getDestination()) != null);
		assertFalse (board.getGame().getGrid().getLocation(move.getCurrent()) == null);
	}
	
	/**
	 * Tests that 
	 * @throws RemoteException
	 */
	@Test
	public void testInvalidMove () throws RemoteException {
		alice.setTurn(true);
		
		/* Move a piece horizontally and validate */
		Checker start = new Checker (2,2, alice.getColor());
		Checker destination = new Checker (3,2, alice.getColor());
		Move move = new Move (board.getGame(), alice, start,destination);
		
		try {
			move = board.validateMove(move);
			fail ("Exception must be thrown!");
		} catch (InvalidMoveException e) {}
		
		try {
			board.move(move);
			fail ("Exception must be thrown!");
		} catch (InvalidMoveException e) {}	
		
		GameGrid grid = board.getGameGrid();
		assertTrue (grid.getLocation (move.getDestination()) == null);
		assertTrue (grid.getLocation (move.getCurrent()) != null);					
	}
	
	public void testJumpMoveValidation () throws RemoteException {
		alice.setTurn (true);
		
		Checker start = new Checker (0,0, alice.getColor());
		Checker stop = new Checker (6,2,alice.getColor());
		
		JumpMove jump = new JumpMove (board.getGame(), alice, start, stop);
		List<Move> moves = composeJumpSequence (jumps);
		for (Move m : moves) {
			jump.addJump(m);
		}
	}

	private List<Move> composeJumpSequence(Checker[] jumps2) {
		// TODO Auto-generated method stub
		return null;
	}
}
