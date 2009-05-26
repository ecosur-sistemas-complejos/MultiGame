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

import java.awt.Point;
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
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.manantiales.Ficha;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesPlayer;
import mx.ecosur.multigame.manantiales.BorderType;
import mx.ecosur.multigame.manantiales.CheckConstraint;
import mx.ecosur.multigame.manantiales.Mode;
import mx.ecosur.multigame.manantiales.TokenType;
import mx.ecosur.multigame.solver.manantiales.SolverFicha;

import org.drools.FactHandle;
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
	
	private ManantialesGame game;
	
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
		charlie = new ManantialesPlayer (game, c, Color.BLACK);
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
	
	@Test
	public void testManantialesCheckConstraintExpired () throws JMSException {
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
		
		FactHandle mh = statefulSession.getFactHandle(move);
		statefulSession.retract(mh);
		
		/* Now have the instigator move */
		charlie.setTurn (true);
		
		SolverFicha terminator = new SolverFicha (1,4, charlie.getColor(), 
				TokenType.MANAGED_FOREST);		
		move = new ManantialesMove (charlie, terminator);
		fireRules (game, move);
		
		assertEquals (GameState.END, game.getState());
		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("END"))
					filter.add(message);					
		}
		
		assertTrue (filter.size() > 0);
		
		filter.clear();
		
		for (CheckConstraint constraint : game.getCheckConstraints()) {
			if (constraint.isExpired())
				filter.add(constraint);
		}
		
		assertTrue (filter.size() > 0);	
		
	}
	
	
	public void testManantialesCheckConstraintRelief() {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,3, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		SolverFicha man2 = new SolverFicha (4,5, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		SolverFicha man3 = new SolverFicha (3,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);		
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);		
		ManantialesMove move = new ManantialesMove (charlie, man3);
		fireRules (game, move);
		
		FactHandle mh = statefulSession.getFactHandle(move);
		statefulSession.retract(mh);
		
		/* Now we have bob fix the first condition and relieve the
		 * checkConstraint
		 */		
		bob.setTurn(true);		
		SolverFicha resolver = new SolverFicha (4,5, bob.getColor(),
				TokenType.MANAGED_FOREST);
		move = new ManantialesMove (bob, resolver);
		fireRules (game, move);
		
		assertEquals (0, game.getCheckConstraints().size());						
	}	
	
	
	@SuppressWarnings("unchecked")
	public void testSouthernBorderDeforestedCheckConstraint () throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,6, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (4,7, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (4,8, charlie.getColor(), 
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
		
		SolverFicha man1 = new SolverFicha (6,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (7,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (8,4, charlie.getColor(), 
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
		
		SolverFicha man1 = new SolverFicha (2,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (1,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (0,4, charlie.getColor(), 
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
		
		SolverFicha man1 = new SolverFicha (4,0, alice.getColor(), 
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
		
		ManantialesMove move = new ManantialesMove ();
		move.setPlayer(alice);
		move.setBadYear(true);
		fireRules (game, move);
		
		assertEquals (Move.Status.UNVERIFIED, move.getStatus());		
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeforestedCheckConstraint () throws JMSException {
		game.setState(GameState.PLAY);
		alice.setTurn(true);
		
		/* Populate board with MODERATE tokens */
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
					/* skip the last ficha */
				if (i == 7 && j==3)
					continue;
				SolverFicha ficha = new SolverFicha (i, j, Color.BLACK,
						TokenType.MODERATE_PASTURE);		
				game.getGrid().updateCell(ficha);
			}
		}
		
		SolverFicha deforest = new SolverFicha (0,5, Color.RED, 
					TokenType.MODERATE_PASTURE);
		ManantialesMove move = new ManantialesMove ();
		move.setPlayer (alice);
		move.setDestination(deforest);
		fireRules (game,move);				
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CHECK_CONSTRAINT"))
					filter.add(message);
		}
		
		assertTrue ("Filter size incorrect! [filter.size==" + filter.size() +"]", 
				filter.size() > 0);
		
		
	}	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeforestedCheckConstraintExpiration() throws JMSException {
		game.setState(GameState.PLAY);
		alice.setTurn(true);
		
		/* Populate board with MODERATE tokens */
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
					/* skip the last ficha */
				if (i == 7 && j==3)
					continue;
				SolverFicha ficha = new SolverFicha (i, j, Color.BLACK,
						TokenType.MODERATE_PASTURE);		
				game.getGrid().updateCell(ficha);
			}
		}
		
		SolverFicha deforest = new SolverFicha (0,5, Color.RED, 
					TokenType.MODERATE_PASTURE);
		ManantialesMove move = new ManantialesMove ();
		move.setPlayer (alice);
		move.setDestination(deforest);
		fireRules (game,move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CHECK_CONSTRAINT"))
					filter.add(message);
		}
		
		assertTrue ("Filter size incorrect! [filter.size==" + filter.size() +"]", 
				filter.size() > 0);
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		
		
		/* Test expiration and consequences */
		
		/* Now have the instigator move */
		alice.setTurn (true);
		
		SolverFicha terminator = new SolverFicha (0,6, alice.getColor(), 
				TokenType.MANAGED_FOREST);		
		move = new ManantialesMove (alice, terminator);
		fireRules (game, move);
		
		assertEquals (GameState.END, game.getState());
		
		filter.clear();
		
		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("END"))
					filter.add(message);					
		}
		
		assertTrue (filter.size() > 0);
		
		filter.clear();
		
		for (CheckConstraint constraint : game.getCheckConstraints()) {
			if (constraint.isExpired())
				filter.add(constraint);
		}
		
		assertTrue (filter.size() > 0);				
	}
	
	@SuppressWarnings("unchecked")
	@Test	
	public void testDeforestedCheckConstraintRelief () throws JMSException {
		game.setState(GameState.PLAY);
		alice.setTurn(true);
		
		/* Populate board with MODERATE tokens */
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
					/* skip the last ficha */
				if (i == 7 && j==3)
					continue;
				SolverFicha ficha = new SolverFicha (i, j, Color.BLACK,
						TokenType.MODERATE_PASTURE);		
				game.getGrid().updateCell(ficha);
			}
		}
		
		SolverFicha deforest = new SolverFicha (0,5, alice.getColor(), 
					TokenType.MODERATE_PASTURE);
		ManantialesMove move = new ManantialesMove ();
		move.setPlayer (alice);
		move.setDestination(deforest);
		fireRules (game,move);				
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CHECK_CONSTRAINT"))
					filter.add(message);
		}
		
		assertTrue ("Filter size incorrect! [filter.size==" + filter.size() +"]", 
				filter.size() > 0);
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		
		
		/* Now, relieve the constraint */
		SolverFicha reforest = new SolverFicha (0, 0, Color.BLACK,
				TokenType.MANAGED_FOREST);
		
		/* Slightly out of turn */
		bob.setTurn(false);			
		
			/* Black Player */
		denise.setTurn(true);		
		move = new ManantialesMove ();
		move.setPlayer (denise);
		move.setDestination(reforest);
		
		fireRules (game,move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (0, game.getCheckConstraints().size());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSouthernBorderRelief() throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,6, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		SolverFicha man2 = new SolverFicha (4,7, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (4,8, charlie.getColor(), 
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
		
		assertTrue (filter.size() > 0);
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		
		
		/* Relieve the constraint */
		denise.setTurn(false);
		alice.setTurn (true);
		
		SolverFicha resolve = new SolverFicha (4,6, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new ManantialesMove (alice, resolve);
		
		fireRules (game,move);
		
		assertEquals (0, game.getCheckConstraints().size());		
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testNorthernBorderRelief() throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,0, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
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
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		
		
		/* Relieve the constraint */
		denise.setTurn(false);
		alice.setTurn (true);
		
		SolverFicha resolve = new SolverFicha (4,0, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new ManantialesMove (alice, resolve);
		
		fireRules (game,move);
		
		assertEquals (0, game.getCheckConstraints().size());			
		
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEasternBorderRelief() throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (6,4, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		SolverFicha man2 = new SolverFicha (7,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (8,4, charlie.getColor(), 
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
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		

		
		/* Relieve the constraint */
		denise.setTurn(false);
		alice.setTurn (true);
		
		SolverFicha resolve = new SolverFicha (6,4, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new ManantialesMove (alice, resolve);
		
		fireRules (game,move);
		
		assertEquals (0, game.getCheckConstraints().size());		
		
	}
	
	@SuppressWarnings("unchecked")
	@Test	
	public void testWesternBorderRelief() throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (2,4, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		SolverFicha man2 = new SolverFicha (1,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (0,4, charlie.getColor(), 
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
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);
		
		/* Relieve the constraint */
		denise.setTurn(false);
		alice.setTurn (true);
		
		SolverFicha resolve = new SolverFicha (2,4, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new ManantialesMove (alice, resolve);
		
		fireRules (game,move);
		
		assertEquals (0, game.getCheckConstraints().size());				
	}
	
	@SuppressWarnings("unchecked")
	@Test	
	public void testWesternBorderExpiration() throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (2,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (1,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (0,4, charlie.getColor(), 
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
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		

		/* Now have the instigator move */
		charlie.setTurn (true);
		
		SolverFicha terminator = new SolverFicha (0,2, charlie.getColor(), 
				TokenType.MANAGED_FOREST);		
		move = new ManantialesMove (charlie, terminator);
		fireRules (game, move);
		
		assertTrue (isTerritoryCleared (BorderType.WEST, game.getGrid()));			
		
	}

	@SuppressWarnings("unchecked")
	@Test	
	public void testEasternBorderExpiration() throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (6,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (7,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (8,4, charlie.getColor(), 
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
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		

		/* Now have the instigator move */
		charlie.setTurn (true);
		
		SolverFicha terminator = new SolverFicha (0,2, charlie.getColor(), 
				TokenType.MANAGED_FOREST);		
		move = new ManantialesMove (charlie, terminator);
		fireRules (game, move);
		
		assertTrue (isTerritoryCleared (BorderType.EAST, game.getGrid()));			
		
	}	

	@SuppressWarnings("unchecked")
	@Test	
	public void testSouthernBorderExpiration() throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,6, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (4,7, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (4,8, charlie.getColor(), 
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
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		

		/* Now have the instigator move */
		charlie.setTurn (true);
		
		SolverFicha terminator = new SolverFicha (0,2, charlie.getColor(), 
				TokenType.MANAGED_FOREST);		
		move = new ManantialesMove (charlie, terminator);
		fireRules (game, move);
		
		assertTrue (isTerritoryCleared (BorderType.SOUTH, game.getGrid()));			
		
	}	

	@SuppressWarnings("unchecked")
	@Test	
	public void testNorthernBorderExpiration() throws JMSException {
		game.setState(GameState.PLAY);
		
		SolverFicha man1 = new SolverFicha (4,0, charlie.getColor(), 
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
		
		/* Only one move at a time, remove the previous move from WM */
		FactHandle handle = this.statefulSession.getFactHandle(move);
		statefulSession.retract(handle);		

		/* Now have the instigator move */
		charlie.setTurn (true);
		
		SolverFicha terminator = new SolverFicha (0,2, charlie.getColor(), 
				TokenType.MANAGED_FOREST);		
		move = new ManantialesMove (charlie, terminator);
		fireRules (game, move);
		
		assertTrue (isTerritoryCleared (BorderType.NORTH, game.getGrid()));
		
	}	
	
	@Test
	public void testStateChange() throws JMSException {
		int intensives = 3, moderates = 6, forested = 3;
		Point [] points = { new Point (0,8), new Point (0,6), new Point (0,4),
				new Point (1,4), new Point (2,4), new Point (3,4), 
				new Point (1,5), new Point (3,5), new Point (2,6),
				new Point (1,7), new Point (3,7), new Point (2,8) };
		
		for (int i = 0; i < 11; i++) {
			SolverFicha ficha = null;			
			if (intensives > 0) {
				ficha = new SolverFicha (points [ i ].x, points [ i ].y, Color.RED,
						TokenType.INTENSIVE_PASTURE);
				intensives--;
				denise.setIntensive(denise.getIntensive() + 1);
			} else if (moderates > 0) {				
				ficha = new SolverFicha (points [ i ].x, points [ i ].y, Color.RED,
						TokenType.MODERATE_PASTURE);				
				moderates--;
				denise.setModerate(denise.getModerate() + 1);
			} else {
				ficha = new SolverFicha (points [ i ].x, points [ i ].y, Color.RED,
						TokenType.MANAGED_FOREST);
				forested--;
				denise.setForested (denise.getForested() + 1);
			}
			
			game.getGrid().updateCell(ficha);
		}
		
		SolverFicha end = new SolverFicha (points [ 11 ].x, points [ 11 ].y, Color.RED,
				TokenType.MANAGED_FOREST);
		
		/* Denise is RED */
		denise.setTurn(true);
		ManantialesMove move = new ManantialesMove (denise, end);
		fireRules (game, move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());				
		assertEquals (24, denise.getScore());
		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("STATE_CHANGE"))
					filter.add(message);
		}
		
		assertTrue (filter.size() > 0);
		
		filter.clear();
		
		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("PLAYER_CHANGE"))
					filter.add(message);
		}
		
		assertTrue (filter.size () > 0);
	}
	
	@Test
	public void testWin () throws JMSException {
		
		game.setMode(Mode.SILVOPASTORAL);

		Point [] points = { new Point (0,8), new Point (0,6), new Point (0,4),
				new Point (1,4), new Point (2,4), new Point (3,4), 
				new Point (1,5), new Point (1,7) };
		
		for (int i = 0; i < 8; i++) {			
			SolverFicha ficha = null;
			ficha = new SolverFicha (points [ i ].x, points [ i ].y, Color.RED,
						TokenType.SILVOPASTORAL);					
			denise.setSilvo(denise.getSilvo() + 1);						
			game.getGrid().updateCell(ficha);
		}
		
		SolverFicha end = new SolverFicha (3,7, Color.RED,
				TokenType.MANAGED_FOREST);		
		/* Denise is RED */
		denise.setTurn(true);
		ManantialesMove move = new ManantialesMove (denise, end);
		fireRules (game, move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());				
		assertEquals (33, denise.getScore());
		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("END"))
					filter.add(message);
		}
		
		assertTrue (filter.size() > 0);	
	}
	
	@Test
	public void testRedBordersDeforested () {
		//fail ();
	}
	
	/**
	 * @param north
	 * @param grid
	 */
	private boolean isTerritoryCleared(BorderType borderType, GameGrid grid) {
		boolean ret  = grid.getCells() != null && grid.getCells().size() > 0;
		for (Cell cell : grid.getCells()) {
			Ficha ficha = (Ficha) cell;
			if (ficha.getBorder().equals(borderType)) {
				ret = false;
				break;
			}			
		}
		return ret;
	}

	private void fireRules(Game game, ManantialesMove move) {
		
		statefulSession.insert(game);
		statefulSession.insert(move);
		for (Object obj : game.getFacts()) {
			statefulSession.insert(obj);
		}
		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();
		statefulSession.setFocus ("move");
		statefulSession.fireAllRules();
		statefulSession.setFocus ("evaluate");
		statefulSession.fireAllRules();
	}	
}
