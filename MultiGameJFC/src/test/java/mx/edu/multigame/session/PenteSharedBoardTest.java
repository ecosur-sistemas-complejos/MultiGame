package mx.edu.multigame.session;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;

import org.junit.Before;
import org.junit.Test;


public class PenteSharedBoardTest {
	
	private SharedBoardRemote board;
	
	private Player alice;
	
	private Player bob;

	@Before
	public void fixtures () throws RemoteException, NamingException, 
		InvalidRegistrationException 
	{
		InitialContext ic = new InitialContext();
		
		RegistrarRemote registrar = (RegistrarRemote) ic.lookup(
			"mx.ecosur.multigame.ejb.RegistrarRemote");
		
		alice = registrar.locatePlayer("alice");
		bob = registrar.locatePlayer("bob");
		
		board = (SharedBoardRemote) ic.lookup(
				"mx.ecosur.multigame.ejb.SharedBoardRemote");
		board.locateSharedBoard(GameType.PENTE);
		
		List<Player> players = board.getPlayers();
		for (Player p : players) {
			registrar.unregisterPlayer(p, GameType.PENTE);
		}
		
		registrar.registerPlayer(alice, GameType.PENTE);
		registrar.registerPlayer(bob, GameType.PENTE);
		
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
	
	

}
