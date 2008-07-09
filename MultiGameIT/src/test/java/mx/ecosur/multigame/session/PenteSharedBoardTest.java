package mx.ecosur.multigame.session;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PenteSharedBoardTest {
	
	private RegistrarRemote registrar;
	
	private SharedBoardRemote board;
	
	private GamePlayer alice, bob, charlie, denise;
	
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
		
		registrar.registerPlayer(registrants [ 0 ], Color.BLACK, GameType.PENTE);
		registrar.registerPlayer(registrants [ 1 ], Color.BLUE, GameType.PENTE);
		registrar.registerPlayer(registrants [ 2 ], Color.GREEN, GameType.PENTE);
		registrar.registerPlayer(registrants [ 3 ], Color.RED, GameType.PENTE);
		
		/* Get the SharedBoard */
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.SharedBoardRemote");
		board.locateSharedBoard(GameType.PENTE);
		
		int row = board.getGame().getRows()/2;
		int column = board.getGame().getColumns()/2;
		
		center = new Cell (row, column, Color.BLACK);
		
		/* Set the GamePlayers from the SharedBoard */
		List<GamePlayer> players = board.getPlayers();
		for (GamePlayer p : players) {
			for (Player r : registrants) {
				if (p.getPlayer().getName().equals("alice"))
					alice = p;
				else if (p.getPlayer().getName().equals("bob"))
					bob = p;
				else if (p.getPlayer().getName().equals("charlie"))
					charlie = p;
				else if (p.getPlayer().getName().equals("denise"))
					denise = p;
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
}
