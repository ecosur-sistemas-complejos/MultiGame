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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;


import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.impl.Color;

import mx.ecosur.multigame.impl.entity.oculto.*;
import mx.ecosur.multigame.impl.enums.oculto.*;
import mx.ecosur.multigame.impl.model.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.drools.io.ResourceFactory;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;

public class OcultoRulesTest extends RulesTestBase {

	private OcultoGame game;

	private OcultoPlayer alice, bob, charlie, denise;

    private static KnowledgeBase oculto;

    /* Setup gente kbase */
    static {
        oculto = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(OcultoGame.class.getResourceAsStream (
            "/mx/ecosur/multigame/impl/oculto.xml")), ResourceType.CHANGE_SET);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        for (KnowledgeBuilderError error : errors) {
            System.out.println (error);
        }
        oculto.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }

	@Before
	public void setUp() throws Exception {

		super.setUp();

		game = new OcultoGame(oculto);
		GridRegistrant[] players = {
				new GridRegistrant ("alice"),
				new GridRegistrant ("bob"),
				new GridRegistrant ("charlie"),
				new GridRegistrant ("denise") };

		Color [] colors = Color.values();
		int counter = 0;

		for (int i = 0; i < colors.length; i++) {
			if (colors [ i ].equals(Color.UNKNOWN) || colors [ i ].equals(Color.GREEN))
					continue;
			game.registerPlayer (players [ counter++ ]);
		}

		for (GridPlayer player : game.getPlayers()) {
			if (player.getRegistrant().getName().equals("alice")) {
				alice = (OcultoPlayer) player;
			} else if (player.getRegistrant().getName().equals("bob")) {
				bob = (OcultoPlayer) player;
			} else if (player.getRegistrant().getName().equals("charlie")) {
				charlie = (OcultoPlayer) player;
			} else if (player.getRegistrant().getName().equals("denise")) {
				denise = (OcultoPlayer)player;
			}
		}
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testInitialize () {

		assertTrue (game.getGrid().getCells().size() == 0);
		Collection<GridPlayer> players = game.getPlayers();
		GridPlayer p = null;
		for (GridPlayer player : players) {
			if (player.getRegistrant().getName().equals("alice"))
				p = player;
		}

		assertNotNull (p);
		assertEquals ("alice", p.getRegistrant().getName());
		assertEquals (true, p.isTurn());
	}

	@Test
	public void testExecuteMove () throws InvalidMoveException {
		alice.setTurn (true);
		game.setState(GameState.PLAY);

		SolverFicha play = new SolverFicha (5, 4, alice.getColor(),
				TokenType.MODERATE_PASTURE);
		OcultoMove move = new OcultoMove (alice, play);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (play, game.getGrid().getLocation(play));

		/* test the scoring */
		assertEquals (1, alice.getModerate());
		assertEquals (2, alice.getScore());
	}

	@Test
	public void testIntensiveMove () throws InvalidMoveException {
		alice.setTurn (true);
		game.setState(GameState.PLAY);

		SolverFicha mod = new SolverFicha (5, 4, alice.getColor(),
				TokenType.MODERATE_PASTURE);
		OcultoMove move = new OcultoMove (alice, mod);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (mod, game.getGrid().getLocation(mod));

		/* test the scoring */
		assertEquals (1, alice.getModerate());
		assertEquals (2, alice.getScore());

		/* Give alice her turn */
		alice.setTurn(true);

		/* Replace the mod with an intensive */
		SolverFicha intensive = new SolverFicha (5,4, alice.getColor(),
				TokenType.INTENSIVE_PASTURE);
		move = new OcultoMove (alice, mod, intensive);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());

		assertEquals(1, alice.getIntensive());
		assertEquals(0, alice.getModerate());
		assertEquals(3, alice.getScore());

	}

	@Test
	public void testRowContiguousIntensiveConstraint () throws InvalidMoveException {
		alice.setTurn (true);
		game.setState(GameState.PLAY);

		SolverFicha contig1 = new SolverFicha (5, 4, alice.getColor(),
				TokenType.INTENSIVE_PASTURE);
		SolverFicha contig2 = new SolverFicha (6, 4, alice.getColor(),
				TokenType.INTENSIVE_PASTURE);

		game.getGrid().updateCell(contig1);
		OcultoMove move = new OcultoMove (alice, contig2);
		game.move (move);

		assertEquals (MoveStatus.INVALID, move.getStatus());
	}

	@Test
	public void testColumnContiguousIntensiveConstraint () throws InvalidMoveException {
		alice.setTurn (true);
		game.setState(GameState.PLAY);

		SolverFicha contig1 = new SolverFicha (5, 4, alice.getColor(),
				TokenType.INTENSIVE_PASTURE);
		SolverFicha contig2 = new SolverFicha (5, 5, alice.getColor(),
				TokenType.INTENSIVE_PASTURE);

		game.getGrid().updateCell(contig1);
		OcultoMove move = new OcultoMove (alice, contig2);
		game.move (move);

		assertEquals (MoveStatus.INVALID, move.getStatus());
	}


	@Test
	public void testDiagonalContiguousIntensiveConstraint() throws InvalidMoveException {
		alice.setTurn (true);
		game.setState(GameState.PLAY);

		SolverFicha contig1 = new SolverFicha (6, 4, alice.getColor(),
				TokenType.INTENSIVE_PASTURE);
		SolverFicha contig2 = new SolverFicha (5, 5, alice.getColor(),
				TokenType.INTENSIVE_PASTURE);

		game.getGrid().updateCell(contig1);
		OcultoMove move = new OcultoMove (alice, contig2);
		game.move (move);

		assertEquals (MoveStatus.INVALID, move.getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testManantialesCheckConstraint () throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("Filter is: " + filter.size(), filter.size() == 1);

	}

    @SuppressWarnings("unchecked")
	@Test
	public void testManantialesCheckConstraintExpired () throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		/* Now have the instigator move */
		bob.setTurn (true);

		SolverFicha terminator = new SolverFicha (1,4, bob.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (bob, terminator);
		game.move (move);

		assertEquals (GameState.ENDED, game.getState());

		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("END"))
					filter.add(message);
		}
		assertTrue (filter.size() > 0);

		filter.clear();
		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}
		assertTrue (filter.size() > 0);

		filter.clear();
		for (CheckCondition constraint : game.getCheckConditions()) {
			if (constraint.isExpired())
				filter.add(constraint);
		}

		assertTrue (filter.size() > 0);

	}

    @SuppressWarnings("unchecked")
	@Test
	public void testManantialesCheckConstraintRelief() throws InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		/* Fix the first condition and relieve the checkConstraint
		 */
		alice.setTurn(true);
		SolverFicha resolver = new SolverFicha (4,3, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (alice, man1, resolver);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (0, game.getCheckConditions().size());
	}


	@SuppressWarnings("unchecked")
    @Test
	public void testSouthernBorderDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITION message intercepted!", filter.size() > 0);

	}

	@SuppressWarnings("unchecked")
    @Test
	public void testEasternBorderDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);

		SolverFicha man1 = new SolverFicha (6,4, bob.getColor(),
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man2 = new SolverFicha (7,4, charlie.getColor(),
				TokenType.INTENSIVE_PASTURE);
		SolverFicha man3 = new SolverFicha (8,4, alice.getColor(),
				TokenType.MODERATE_PASTURE);
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		OcultoMove move = new OcultoMove (alice, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITION message intercepted!",filter.size() != 0);
		assertTrue ("Filter contains: " + filter.size(), filter.size() == 1);

	}

	@SuppressWarnings("unchecked")
    @Test
	public void testWesternBorderDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITION message intercepted!",filter.size() > 0);

	}

	@SuppressWarnings("unchecked")
    @Test
	public void testNorthernBorderDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITION message intercepted!", filter.size() > 0);
		assertTrue ("Filter.size()==" + filter.size(), filter.size() == 1);

	}

    @SuppressWarnings("unchecked")
	@Test
	public void testBadYear () throws InvalidMoveException {
		alice.setTurn (true);
		game.setState(GameState.PLAY);

		OcultoMove move = new OcultoMove ();
		move.setPlayer(alice);
		move.setBadYear(true);
		game.move (move);

		assertEquals (MoveStatus.UNVERIFIED, move.getStatus());
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove ();
		move.setPlayer (alice);
		move.setDestinationCell(deforest);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		assertTrue ("Condition not found!", filter.size() > 0);
		assertTrue ("Filter size incorrect! [filter.size==" + filter.size() +"]",
				filter.size() == 1);


	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeforestedCheckConstraintExpiration() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove ();
		move.setPlayer (alice);
		move.setDestinationCell(deforest);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		assertTrue ("Filter size incorrect! [filter.size==" + filter.size() +"]",
				filter.size() > 0);

		/* Test expiration and consequences */

		/* Now have the previous player move */
		charlie.setTurn (true);

		SolverFicha terminator = new SolverFicha (0,6, charlie.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (denise, terminator);
		game.move (move);

		assertEquals (GameState.ENDED, game.getState());

		filter.clear();

		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("END"))
					filter.add(message);
		}

		assertTrue (filter.size() > 0);

		filter.clear();
		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}

        assertTrue (filter.size() > 0);

        filter.clear();
		for (CheckCondition constraint : game.getCheckConditions()) {
			if (constraint.isExpired())
				filter.add(constraint);
		}

		assertTrue (filter.size() > 0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeforestedCheckConstraintRelief () throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove ();
		move.setPlayer (alice);
		move.setDestinationCell(deforest);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		assertTrue ("Condition not raised!", filter.size() > 0);
		assertTrue ("Filter size incorrect! [filter.size==" + filter.size() +"]",
				filter.size() == 1);

		/* Now, relieve the constraint */
		SolverFicha reforest = new SolverFicha (0, 5, alice.getColor(),
				TokenType.MANAGED_FOREST);

		bob.setTurn(false);
		alice.setTurn(true);
		move = new OcultoMove (alice, deforest, reforest);

		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (0, game.getCheckConditions().size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSouthernBorderRelief() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		assertTrue ("No RAISED_CONDITION message intercepted!",filter.size() > 0);

		/* Relieve the constraint */
		denise.setTurn(false);
		alice.setTurn (true);

		SolverFicha resolve = new SolverFicha (4,6, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (alice, man3, resolve);

		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (0, game.getCheckConditions().size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNorthernBorderRelief() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITION message intercepted!",filter.size() > 0);

		/* Relieve the constraint */
		denise.setTurn(false);
		alice.setTurn (true);

		SolverFicha resolve = new SolverFicha (4,0, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (alice, man3, resolve);

		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (0, game.getCheckConditions().size());


	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEasternBorderRelief() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue (filter.size() > 0);


		/* Relieve the constraint */
		denise.setTurn(false);
		alice.setTurn (true);

		SolverFicha resolve = new SolverFicha (6,4, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (alice, resolve);

		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (0, game.getCheckConditions().size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWesternBorderRelief() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITION message intercepted!",filter.size() > 0);

		/* Relieve the constraint */
		denise.setTurn(false);
		alice.setTurn (true);

		SolverFicha resolve = new SolverFicha (2,4, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (alice, resolve);

		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (0, game.getCheckConditions().size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWesternBorderExpiration() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITON message intercepted!", filter.size() > 0);

		/* Now have the instigator move */
		bob.setTurn (true);

		SolverFicha terminator = new SolverFicha (0,2, bob.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (bob, terminator);
		game.move (move);

		assertTrue (isTerritoryCleared (BorderType.WEST, game.getGrid()));

		filter.clear();
		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}

        assertTrue (filter.size() > 0);

        filter.clear();

        /* Ensure that there are no MODERATE or INTENSIVE fichas on the Border */
        for (GridCell cell : game.getGrid().getCells()) {
            SolverFicha ficha = (SolverFicha) cell;
            if (ficha.getBorder().equals(BorderType.WEST))
                filter.add(ficha);
        }

        assertTrue (filter.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEasternBorderExpiration() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue (filter.size() > 0);

		/* Now have the instigator move */
		bob.setTurn (true);

		SolverFicha terminator = new SolverFicha (0,2, bob.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (bob, terminator);
		game.move (move);

		assertTrue (isTerritoryCleared (BorderType.EAST, game.getGrid()));

		filter.clear();
		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}

        assertTrue (filter.size() > 0);
        filter.clear();

        /* Ensure that there are no MODERATE or INTENSIVE fichas on the Border */
        for (GridCell cell : game.getGrid().getCells()) {
            SolverFicha ficha = (SolverFicha) cell;
            if (ficha.getBorder().equals(BorderType.EAST))
                filter.add(ficha);
        }

        assertTrue (filter.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSouthernBorderExpiration() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITION message intercepted!",filter.size() > 0);

		bob.setTurn (true);

		SolverFicha terminator = new SolverFicha (0,2, bob.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (bob, terminator);
		game.move (move);

		assertTrue (isTerritoryCleared (BorderType.SOUTH, game.getGrid()));

		filter.clear();
		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}

        assertTrue (filter.size() > 0);

        filter.clear();

        /* Ensure that there are no MODERATE or INTENSIVE fichas on the Border */
        for (GridCell cell : game.getGrid().getCells()) {
            SolverFicha ficha = (SolverFicha) cell;
            if (ficha.getBorder().equals(BorderType.SOUTH))
                filter.add(ficha);
        }

        assertTrue (filter.size() == 0);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNorthernBorderExpiration() throws JMSException, InvalidMoveException {
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
		OcultoMove move = new OcultoMove (charlie, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		/* Should only be one message */
		assertTrue ("No RAISED_CONDITION message intercepted!",filter.size() > 0);

		bob.setTurn (true);

		SolverFicha terminator = new SolverFicha (0,2, bob.getColor(),
				TokenType.MANAGED_FOREST);
		move = new OcultoMove (bob, terminator);
		game.move (move);

		assertTrue (isTerritoryCleared (BorderType.NORTH, game.getGrid()));

		filter.clear();
		messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}


        assertTrue (filter.size() > 0);

        filter.clear();

        /* Ensure that there are no MODERATE or INTENSIVE fichas on the Border */
        for (GridCell cell : game.getGrid().getCells()) {
            SolverFicha ficha = (SolverFicha) cell;
            if (ficha.getBorder().equals(BorderType.NORTH))
                filter.add(ficha);
        }

        assertTrue (filter.size() == 0);

	}

	/*
	 *
	 */
    @SuppressWarnings("unchecked")
	@Test
	public void testReplaceModerateWithIntensiveOnManantial() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);

		SolverFicha man1 = new SolverFicha (4,3, bob.getColor(),
				TokenType.MODERATE_PASTURE);
		SolverFicha man2 = new SolverFicha (4,5, bob.getColor(),
				TokenType.MODERATE_PASTURE);
		SolverFicha man3 = new SolverFicha (4,5, bob.getColor(),
				TokenType.INTENSIVE_PASTURE);
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);

		bob.setTurn(true);
		OcultoMove move = new OcultoMove ();
		move.setPlayer(bob);
		move.setCurrentCell(man2);
		move.setDestinationCell(man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		assertTrue ("CONDITION RAISED ON CONVERSION!", filter.size() == 0);
	}

    @SuppressWarnings("unchecked")
    @Test
	public void testReplaceModerateWithIntensiveOnBorder () throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);

		SolverFicha man1 = new SolverFicha (0,4, bob.getColor(),
				TokenType.MODERATE_PASTURE);
		SolverFicha man2 = new SolverFicha (1,4, bob.getColor(),
				TokenType.MODERATE_PASTURE);
		SolverFicha man3 = new SolverFicha (0,4, bob.getColor(),
				TokenType.INTENSIVE_PASTURE);
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);

		bob.setTurn(true);
		OcultoMove move = new OcultoMove (bob, man1, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		assertTrue ("CONDITION RAISED ON CONVERSION!", filter.size() == 0);
	}

    @SuppressWarnings("unchecked")
    @Test
	public void testReplaceModerateWithIntensiveOnBorderWithPopulatedBorders ()
		throws JMSException, InvalidMoveException
	{
		game.setState(GameState.PLAY);

		SolverFicha man1 = new SolverFicha (0,4, bob.getColor(),
				TokenType.MODERATE_PASTURE);
		SolverFicha man2 = new SolverFicha (1,4, bob.getColor(),
				TokenType.MODERATE_PASTURE);
		SolverFicha man3 = new SolverFicha (0,4, bob.getColor(),
				TokenType.INTENSIVE_PASTURE);
		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);

		/* Set MODERATES on the other borders */
		SolverFicha ficha = new SolverFicha (4,0, alice.getColor(), TokenType.MODERATE_PASTURE);
		game.getGrid().updateCell (ficha);
		ficha = new SolverFicha (4,1, alice.getColor(), TokenType.MODERATE_PASTURE);
		game.getGrid().updateCell(ficha);

		ficha = new SolverFicha (8,4, charlie.getColor(), TokenType.MODERATE_PASTURE);
		game.getGrid().updateCell(ficha);
		ficha = new SolverFicha (7,4, charlie.getColor(), TokenType.MODERATE_PASTURE);
		game.getGrid().updateCell(ficha);

		/* Convert Moderate to Intensive */
		bob.setTurn(true);
		OcultoMove move = new OcultoMove (bob, man1, man3);
		game.move (move);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		ArrayList filter = new ArrayList();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_RAISED"))
					filter.add(message);
		}

		assertTrue ("CONDITION(S) RAISED ON CONVERSION!  Conditions Raised: " + filter.size(),
				filter.size() == 0);
	}

	/**
	 * @param borderType
	 * @param grid
	 */
	private boolean isTerritoryCleared(BorderType borderType, GameGrid grid) {
		boolean ret  = grid.getCells() != null && grid.getCells().size() > 0;
		for (GridCell cell : grid.getCells()) {
			Ficha ficha = (Ficha) cell;
			if (ficha.getBorder().equals(borderType)) {
				ret = false;
				break;
			}
		}
		return ret;
	}
}