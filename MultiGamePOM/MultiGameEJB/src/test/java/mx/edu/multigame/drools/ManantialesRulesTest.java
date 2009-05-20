/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.edu.multigame.drools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesPlayer;
import mx.ecosur.multigame.manantiales.TokenType;
import mx.ecosur.multigame.solver.manantiales.SolverFicha;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ManantialesRulesTest extends RulesTestBase {
	
	private static RuleBase Ruleset;
	
	private static boolean DEBUG = false;
	
	private Game game;
	
	private ManantialesPlayer alice, bob, charlie, denise;

	private StatefulSession statefulSession;
	
	/** Static Initializer only loads rules once from the file system */
	static {
		PackageBuilder builder = new PackageBuilder();
		InputStreamReader reader = new InputStreamReader(ManantialesRulesTest.class
				.getResourceAsStream("/mx/ecosur/multigame/manantiales.drl"));
		try {
			builder.addPackageFromDrl(reader);
			Ruleset = RuleBaseFactory.newRuleBase();
			Ruleset.addPackage( builder.getPackage() );
		} catch (DroolsParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUp() throws Exception {

		super.setUp();
		
		game = new ManantialesGame();
		game.initialize(GameType.MANANTIALES);
		statefulSession = Ruleset.newStatefulSession();
		if (DEBUG)
			statefulSession.addEventListener(new DebugEventListener());
		Player a, b, c, d;
		a = new Player ("alice");
		b = new Player ("bob");
		c = new Player ("charlie");
		d = new Player ("denise");
		
		alice = new ManantialesPlayer (game, a, Color.YELLOW);
		bob = new ManantialesPlayer (game, b, Color.BLUE);
		charlie = new ManantialesPlayer (game, c, Color.GREEN);
		denise = new ManantialesPlayer (game, d, Color.RED);
		
		game.addPlayer(alice);
		game.addPlayer(bob);
		game.addPlayer(charlie);
		game.addPlayer(denise);
		
		statefulSession.insert(game);
		statefulSession.setFocus("initialize");
		statefulSession.fireAllRules();
	}
	
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		statefulSession.dispose();
	}
	
	@Test
	public void testInitialize () {
		game = new ManantialesGame();
		game.initialize(GameType.MANANTIALES);
		game.setState(GameState.BEGIN);
		
		game.addPlayer(alice);
		game.addPlayer(bob);
		game.addPlayer(charlie);
		game.addPlayer(denise);		
		
		statefulSession.insert(game);
		statefulSession.setFocus("initialize");
		statefulSession.fireAllRules();
	
		assertTrue (game.getGrid().getCells().size() == 0);
		List<GamePlayer> players = game.getPlayers();
		
		GamePlayer p = players.get(players.indexOf(alice));
		assertNotNull (p);
		assertEquals ("alice", p.getPlayer().getName());
		assertEquals (true, p.isTurn());
	}
	
	@Test
	public void testValidateMove () {
		alice.setTurn (true);
		game.setState(GameState.PLAY);
		
		SolverFicha play = new SolverFicha (5, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		ManantialesMove move = new ManantialesMove (alice, play);
		
		statefulSession.insert(game);
		statefulSession.insert(move);
		statefulSession.insert(game.getGrid().getCells());
		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();		
		assertEquals (Move.Status.VERIFIED, move.getStatus());		
	}	
	
	@Test
	public void testExecuteMove () {
		alice.setTurn (true);
		game.setState(GameState.PLAY);
		
		SolverFicha play = new SolverFicha (5, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		ManantialesMove move = new ManantialesMove (alice, play);
		fireRules (game, move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (play, game.getGrid().getLocation(play));
		
		/* test the scoring */
		assertEquals (1, alice.getIntensive());
		assertEquals (3, alice.getScore());
	}
	
	@Test
	public void testRowContiguousIntensiveConstraint () {
		alice.setTurn (true);
		game.setState(GameState.PLAY);
		
		SolverFicha contig1 = new SolverFicha (5, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha contig2 = new SolverFicha (6, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		
		game.getGrid().updateCell(contig1);
		ManantialesMove move = new ManantialesMove (alice, contig2);
		fireRules (game, move);
		
		assertEquals (Move.Status.INVALID, move.getStatus());
	}
	
	@Test
	public void testColumnContiguousIntensiveConstraint () {
		alice.setTurn (true);
		game.setState(GameState.PLAY);

		SolverFicha contig1 = new SolverFicha (5, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha contig2 = new SolverFicha (5, 5, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		
		game.getGrid().updateCell(contig1);
		ManantialesMove move = new ManantialesMove (alice, contig2);
		fireRules (game, move);
		
		assertEquals (Move.Status.INVALID, move.getStatus());
	}
	

	@Test
	public void testDiagonalContiguousIntensiveConstraint() {
		alice.setTurn (true);
		game.setState(GameState.PLAY);
		
		SolverFicha contig1 = new SolverFicha (6, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha contig2 = new SolverFicha (5, 5, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		
		game.getGrid().updateCell(contig1);
		ManantialesMove move = new ManantialesMove (alice, contig2);
		fireRules (game, move);
		
		assertEquals (Move.Status.INVALID, move.getStatus());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testManantialesCheckConstraint () throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,3, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (4,5, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (3,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);		
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);		
		ManantialesMove move = new ManantialesMove (charlie, man3);
		fireRules (game, move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CHECK_CONSTRAINT"))
					filter.add(message);
		}
		
		/* Should only be one message */
		assertTrue (filter.size() > 0);
		
	}
	
	@SuppressWarnings("unchecked")
	public void testSouthernBorderDeforestedCheckConstraint () throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,5, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (4,7, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (4,6, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);		
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
		fireRules (game, move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CHECK_CONSTRAINT"))
					filter.add(message);
		}
		
		/* Should only be one message */
		assertTrue (filter.size() > 0);
		
	}
	
	@SuppressWarnings("unchecked")
	public void testEasternBorderDeforestedCheckConstraint () throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (5,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (7,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (6,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);		
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
		fireRules (game, move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CHECK_CONSTRAINT"))
					filter.add(message);
		}
		
		/* Should only be one message */
		assertTrue (filter.size() > 0);
		
	}
	
	@SuppressWarnings("unchecked")
	public void testWesternBorderDeforestedCheckConstraint () throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (3,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (1,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (2,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);		
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
		fireRules (game, move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CHECK_CONSTRAINT"))
					filter.add(message);
		}
		
		/* Should only be one message */
		assertTrue (filter.size() > 0);
		
	}
	
	@SuppressWarnings("unchecked")
	public void testNorthernBorderDeforestedCheckConstraint () throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,3, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (4,1, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (4,2, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);		
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
		fireRules (game, move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CHECK_CONSTRAINT"))
					filter.add(message);
		}
		
		/* Should only be one message */
		assertTrue (filter.size() > 0);
		
	}	
	
	@Test
	public void testBadYear () {
		alice.setTurn (true);
		game.setState(GameState.PLAY);
		
		SolverFicha play = new SolverFicha (-1, -1, alice.getColor(), 
				TokenType.UNKNOWN);
		
		ManantialesMove move = new ManantialesMove ();
		move.setPlayer(alice);
		move.setBadYear(true);
		//move.setDestination(play);
		fireRules (game, move);
		
		assertEquals (Move.Status.UNVERIFIED, move.getStatus());		
	}
	
	private void fireRules(Game game, ManantialesMove move) {
		statefulSession.insert(game);
		statefulSession.insert(move);
		for (Cell cell : game.getGrid().getCells()) {
			statefulSession.insert(cell);
		}
		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();
		statefulSession.setFocus ("move");
		statefulSession.fireAllRules();
		statefulSession.setFocus ("evaluate");
		statefulSession.fireAllRules();
	}	
}
