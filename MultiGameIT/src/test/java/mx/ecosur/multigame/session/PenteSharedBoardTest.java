package mx.ecosur.multigame.session;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;
import mx.ecosur.multigame.ejb.entity.pente.PentePlayer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PenteSharedBoardTest {
	
	private RegistrarRemote registrar;
	
	private SharedBoardRemote board;
	
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
		
		registrar.registerPlayer(registrants [ 0 ], Color.YELLOW, GameType.PENTE);
		registrar.registerPlayer(registrants [ 1 ], Color.BLUE, GameType.PENTE);
		registrar.registerPlayer(registrants [ 2 ], Color.GREEN, GameType.PENTE);
		registrar.registerPlayer(registrants [ 3 ], Color.RED, GameType.PENTE);
		
		/* Get the SharedBoard */
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.SharedBoardRemote");
		board.locateSharedBoard(GameType.PENTE);
		
		int row = board.getGame().getRows()/2;
		int column = board.getGame().getColumns()/2;
		
		center = new Cell (row, column, Color.YELLOW);
		
		/* Set the GamePlayers from the SharedBoard */
		List<GamePlayer> players = board.getPlayers();
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
		assertTrue (board.getGameGrid().getCells().size() == 0);
	}
	
	/** 
	 * Tests the first move logic.  This tests the positive condition.
	 * @throws InvalidMoveException 
	 */
	@Test
	public void testFirstMoveValidate () throws InvalidMoveException {
		PenteMove move = new PenteMove (alice, center);
		Move ret = board.validateMove(move);
		assertEquals (Move.Status.VERIFIED, ret.getStatus());
	}
	
	/** 
	 * Tests the first move logic.  This tests the positive condition.
	 * @throws InvalidMoveException 
	 * @throws RemoteException 
	 */
	@Test
	public void testFirstMove () throws InvalidMoveException, RemoteException {
		PenteMove move = new PenteMove (alice, center);
		Move validMove = board.validateMove(move);
		board.move(validMove);
		
		GameGrid grid = board.getGameGrid();
		assertNotNull (grid.getLocation(validMove.getDestination()));
	}	

	/**
	 * Tests the first move logic.  This tests the negative condition.
	 */
	@Test
	public void testBadFirstMove () {
		int row = center.getRow() -1;
		int col = center.getColumn() + 1;
		
		Cell center = new Cell (row, col, alice.getColor());
		PenteMove move = new PenteMove (alice, center);

		try {
			Move invalid = board.validateMove(move);
			fail ("Invalid Move must be thrown!");
		} catch (InvalidMoveException e) {
			assertTrue ("Invalid Move!".equals(e.getMessage()));
		} 
	}
	
	@Test
	public void testFormTria () throws InvalidMoveException, RemoteException {
		/* Round 1 */
		
		PenteMove move = new PenteMove (alice, center);
		Move valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		Cell cell = new Cell (1, 1, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3,1, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5,1, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Round 2 */
		
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 1, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (1, 2, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3,2, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 2, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		
		/* Round 3 */
		
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 2, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (1, 3, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		move = (PenteMove) valid;
		
		assertEquals (1, move.getTrias().size());
		
		List<GamePlayer> players = board.getPlayers();
		for (GamePlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (PentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTrias().size());
	}
	
	@Test
	public void testSelfishScoring () throws InvalidMoveException, RemoteException {
		
		/* Round 1 */
		PenteMove move = new PenteMove (alice, center);
		Move valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		Cell cell = new Cell (1, 1, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3,1, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5,1, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Round 2 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 1, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (1, 2, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3,2, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 2, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		
		/* Round 3 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 2, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (1, 3, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		move = (PenteMove) valid;
		
		assertEquals (1, move.getTrias().size());
		
		List<GamePlayer> players = board.getPlayers();
		for (GamePlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (PentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTrias().size());
		bob.setTurn(true);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3,3, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 3, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Round 4 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 3, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (9,1, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3, 5, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 5, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Round 5 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 5, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (9, 2, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3, 6, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 6, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Round 6 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 6, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (9,3, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Game should be over, with Bob the winner */
		assertEquals (GameState.END, board.getState()); 
		PenteGame pente = (PenteGame) board.getGame();
		
		Set <PentePlayer> winners = pente.getWinners();
		
		assertTrue (winners.size() == 1);
		for (PentePlayer player: winners) {
			assertEquals (bob.getId(), player.getId());
			assertEquals (player.getPoints(), 10);
		}
	}
	
	@Test
	public void testCooperativeScoring () throws InvalidMoveException, RemoteException {
		/* Round 1 */
		PenteMove move = new PenteMove (alice, center);
		Move valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		Cell cell = new Cell (1, 1, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3,1, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5,1, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Round 2 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 1, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (1, 2, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3,2, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 2, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		
		/* Round 3 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 2, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (3, 3, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		move = (PenteMove) valid;
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (1, 3, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 3, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Assert that Denise has completed a tria */
		assertEquals (1, move.getTrias().size());
		
		List<GamePlayer> players = board.getPlayers();
		for (GamePlayer player : players) {
			if (! (player.getId() == denise.getId()))
				continue;
			denise = (PentePlayer) player;
			break;
		}
		
		assertEquals (1, denise.getTrias().size());
		denise.setTurn(true);
		
		/* Round 4 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 3, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (1,4, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Assert that Bob has completed a tessera */
		assertEquals (1, move.getTesseras().size());
		
		players = board.getPlayers();
		for (GamePlayer player : players) {
			if (! (player.getId() == bob.getId()))
				continue;
			bob = (PentePlayer) player;
			break;
		}
		
		assertEquals (1, bob.getTesseras().size());
		bob.setTurn(true);
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (3, 4, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		/* Assert that Charlie has completed a tessera */
		assertEquals (1, move.getTesseras().size());
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 5, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Round 5 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 5, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (0, 3, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		move = (PenteMove) valid;
		
		charlie = (PentePlayer) board.incrementTurn(bob);
		cell = new Cell (2, 5, charlie.getColor());
		move = new PenteMove (charlie, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		denise = (PentePlayer) board.incrementTurn(charlie);
		cell = new Cell (5, 7, denise.getColor());
		move = new PenteMove (denise, cell);
		valid = board.validateMove (move);
		board.move(valid);
		
		/* Round 6 */
		alice = (PentePlayer) board.incrementTurn(denise);
		cell = new Cell (7, 6, alice.getColor());
		move = new PenteMove (alice, cell);
		valid = board.validateMove(move);
		board.move(valid);
		
		bob = (PentePlayer) board.incrementTurn(alice);
		cell = new Cell (3, 6, bob.getColor());
		move = new PenteMove (bob, cell);
		valid = board.validateMove (move);
		board.move(valid);
		move = (PenteMove) valid;
		
		/* Confirm cooperative scoring */
		
		players = board.getPlayers();
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
		assertEquals (GameState.END, board.getState()); 
		PenteGame pente = (PenteGame) board.getGame();
		
		Set <PentePlayer> winners = pente.getWinners();
		assertEquals (2, winners.size());
		for (PentePlayer player: winners) {
			assertEquals (player.getPoints(), 5);
			assertTrue (bob.getId() == player.getId() || charlie.getId() == player.getId());
		}
	}
}
