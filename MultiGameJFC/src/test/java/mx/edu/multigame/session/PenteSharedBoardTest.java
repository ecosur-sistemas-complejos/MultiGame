package mx.edu.multigame.session;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PenteSharedBoardTest {
	
	private RegistrarRemote registrar;
	
	private SharedBoardRemote board;
	
	private Player alice, bob, charlie, denise;
	

	@Before
	public void fixtures () throws RemoteException, NamingException, InvalidRegistrationException {
		InitialContext ic = new InitialContext();
		
		registrar = (RegistrarRemote) ic.lookup(
			"mx.ecosur.multigame.ejb.RegistrarRemote");
		
		alice = registrar.locatePlayer("alice");
		bob = registrar.locatePlayer("bob");
		charlie = registrar.locatePlayer("charlie");
		denise = registrar.locatePlayer("denise");
		
		registrar.registerPlayer(alice, GameType.PENTE);
		registrar.registerPlayer(bob, GameType.PENTE);
		registrar.registerPlayer(charlie, GameType.PENTE);
		registrar.registerPlayer(denise, GameType.PENTE);
		
		/* Get the SharedBoard */
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.SharedBoardRemote");
		board.locateSharedBoard(GameType.PENTE, alice);	
	}
	
	@After
	public void tearDown () throws NamingException, RemoteException, InvalidRegistrationException {
		if (board != null && registrar != null) {
			List<Player> players = board.getPlayers();
			for (Player p : players) {
				registrar.unregisterPlayer(p, GameType.PENTE);
			}
		} else {
			System.out.println ("Null board or registrar! board [" + board + 
					"], registrar [" + registrar + "]");
		}
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
		Cell center = new Cell (10, 10, alice.getColor());
		PenteMove move = new PenteMove (board.getGame(), alice, center);
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
		Cell center = new Cell (10,10,alice.getColor());
		PenteMove move = new PenteMove (board.getGame(), alice, center);
		Move validMove = board.validateMove(move);
		assertEquals (Move.Status.VERIFIED, validMove.getStatus());
		board.move(validMove);
		
		GameGrid grid = board.getGameGrid();
		assertNotNull (grid.getLocation(validMove.getDestination()));
	}	

	/**
	 * Tests the first move logic.  This tests the negative condition.
	 */
	
	public void testBadFirstMove () {
		Cell center = new Cell (9,9,alice.getColor());
		PenteMove move = new PenteMove (board.getGame(), alice, center);
		Exception exception = null;
		try {
			board.validateMove(move);
			fail ("Invalid Move must be thrown!");
		} catch (InvalidMoveException e) {
			assertTrue ("Invalid Move!".equals(e.getMessage()));
			exception = e;
		}
		
		assertNotNull(exception);	
	}
}
