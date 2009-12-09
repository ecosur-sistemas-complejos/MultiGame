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
import java.util.*;

import javax.jms.JMSException;
import javax.jms.Message;


import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.impl.Color;

import mx.ecosur.multigame.impl.entity.manantiales.CheckCondition;
import mx.ecosur.multigame.impl.entity.manantiales.Ficha;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesPlayer;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;

import mx.ecosur.multigame.impl.enums.manantiales.BorderType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;

import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.drools.io.ResourceFactory;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;

public class ManantialesRulesTest extends RulesTestBase {
	
	private ManantialesGame game;
	
	private ManantialesPlayer alice, bob, charlie, denise;

    private static KnowledgeBase manantiales;

    /* Setup gente kbase */
    static {
        manantiales = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(GenteGame.class.getResourceAsStream (
            "/mx/ecosur/multigame/impl/manantiales.xml")), ResourceType.CHANGE_SET);
        manantiales.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }
	
	@Before
	public void setUp() throws Exception {

		super.setUp();

		game = new ManantialesGame(manantiales);
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
				alice = (ManantialesPlayer) player;				
			} else if (player.getRegistrant().getName().equals("bob")) {
				bob = (ManantialesPlayer) player;
			} else if (player.getRegistrant().getName().equals("charlie")) {
				charlie = (ManantialesPlayer) player;
			} else if (player.getRegistrant().getName().equals("denise")) {
				denise = (ManantialesPlayer)player;
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
		
		Ficha play = new Ficha (5, 4, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
        setIds(play);
		ManantialesMove move = new ManantialesMove (alice, play);
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
		
		Ficha mod = new Ficha (5, 4, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha intensive = new Ficha (5,4, alice.getColor(),
				TokenType.INTENSIVE_PASTURE);
        setIds (mod, intensive);

		ManantialesMove move = new ManantialesMove (alice, mod);
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (mod, game.getGrid().getLocation(mod));
		
		/* test the scoring */
		assertEquals (1, alice.getModerate());
		assertEquals (2, alice.getScore());			
		
		/* Give alice her turn */
		alice.setTurn(true);
		
		/* Replace the mod with an intensive */
		move = new ManantialesMove (alice, mod, intensive);
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
		
		Ficha contig1 = new Ficha (5, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha contig2 = new Ficha (6, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		setIds (contig1, contig2);

		game.getGrid().updateCell(contig1);
		ManantialesMove move = new ManantialesMove (alice, contig2);
		game.move (move);
		
		assertEquals (MoveStatus.INVALID, move.getStatus());
	}
	
	@Test
	public void testColumnContiguousIntensiveConstraint () throws InvalidMoveException {
		alice.setTurn (true);
		game.setState(GameState.PLAY);

		Ficha contig1 = new Ficha (5, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha contig2 = new Ficha (5, 5, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		setIds (contig1, contig2);

		game.getGrid().updateCell(contig1);
		ManantialesMove move = new ManantialesMove (alice, contig2);
		game.move (move);
		
		assertEquals (MoveStatus.INVALID, move.getStatus());
	}
	

	@Test
	public void testDiagonalContiguousIntensiveConstraint() throws InvalidMoveException {
		alice.setTurn (true);
		game.setState(GameState.PLAY);
		
		Ficha contig1 = new Ficha (6, 4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha contig2 = new Ficha (5, 5, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
        setIds (contig1, contig2);
		
		game.getGrid().updateCell(contig1);
		ManantialesMove move = new ManantialesMove (alice, contig2);
		game.move (move);
		
		assertEquals (MoveStatus.INVALID, move.getStatus());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testManantialesCheckConstraint () throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (4,3, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (4,5, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (3,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
        setIds (man1, man2, man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);		
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		
		Ficha man1 = new Ficha (4,3, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (4,5, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (3,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
        setIds (man1, man2, man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);		
		ManantialesMove move = new ManantialesMove (charlie, man3);
		game.move (move);
		
		/* Now have the instigator move */
		bob.setTurn (true);
		
		Ficha terminator = new Ficha (1,4, bob.getColor(), 
				TokenType.MANAGED_FOREST);		
		move = new ManantialesMove (bob, terminator);
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
		
		Ficha man1 = new Ficha (4,3, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man2 = new Ficha (4,5, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man3 = new Ficha (3,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
        setIds (man1, man2, man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);		
		ManantialesMove move = new ManantialesMove (charlie, man3);
		game.move (move);
		
		/* Fix the first condition and relieve the checkConstraint
		 */		
		alice.setTurn(true);		
		Ficha resolver = new Ficha (4,3, alice.getColor(),
				TokenType.MANAGED_FOREST);
		move = new ManantialesMove (alice, man1, resolver);
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());			
		assertEquals (0, game.getCheckConditions().size());						
	}	
	
	
	@SuppressWarnings("unchecked")
    @Test
	public void testSouthernBorderDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (4,6, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (4,7, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (4,8, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
        setIds (man1, man2, man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		
		Ficha man1 = new Ficha (6,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (7,4, charlie.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (8,4, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
        setIds (man1, man2,man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		ManantialesMove move = new ManantialesMove (alice, man3);
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
		
		Ficha man1 = new Ficha (2,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (1,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (0,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
        setIds (man1, man2, man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		
		Ficha man1 = new Ficha (4,0, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (4,1, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (4,2, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
        setIds (man1, man2, man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		
		ManantialesMove move = new ManantialesMove ();
		move.setPlayer(alice);
		move.setBadYear(true);
		game.move (move);
		
		assertEquals (MoveStatus.UNVERIFIED, move.getStatus());		
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
        List<Ficha> fichas = new LinkedList<Ficha>();

		/* Populate board with MODERATE tokens */
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
					/* skip the last ficha */
				if (i == 7 && j==3)
					continue;
				Ficha ficha = new Ficha (i, j, Color.BLACK,
						TokenType.MODERATE_PASTURE);		
				fichas.add(ficha);
			}
		}

        Ficha deforest = new Ficha (0,5, Color.RED,
					TokenType.MODERATE_PASTURE);
        fichas.add(deforest);
        Ficha[] fichaArr = fichas.toArray(new Ficha[] {});
        setIds (fichaArr);
        fichas.remove(deforest);

        for (Ficha ficha : fichas) {
            game.getGrid().updateCell(ficha);
        }
        
		ManantialesMove move = new ManantialesMove ();
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
		
		List<Ficha> fichas = new LinkedList<Ficha>();

		/* Populate board with MODERATE tokens */
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
					/* skip the last ficha */
				if (i == 7 && j==3)
					continue;
				Ficha ficha = new Ficha (i, j, Color.BLACK,
						TokenType.MODERATE_PASTURE);
				fichas.add(ficha);
			}
		}

        Ficha deforest = new Ficha (0,5, Color.RED,
					TokenType.MODERATE_PASTURE);
        fichas.add(deforest);
        setIds (fichas.toArray(new Ficha[] {}));
        fichas.remove(deforest);

        for (Ficha ficha : fichas) {
            game.getGrid().updateCell(ficha);  
        }

		ManantialesMove move = new ManantialesMove ();
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
		
		Ficha terminator = new Ficha (0,6, charlie.getColor(), 
				TokenType.MANAGED_FOREST);		
		move = new ManantialesMove (denise, terminator);
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
		
		List<Ficha> fichas = new LinkedList<Ficha>();

		/* Populate board with MODERATE tokens */
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
					/* skip the last ficha */
				if (i == 7 && j==3)
					continue;
				Ficha ficha = new Ficha (i, j, Color.BLACK,
						TokenType.MODERATE_PASTURE);
				fichas.add(ficha);
			}
		}

        Ficha deforest = new Ficha (0,5, Color.RED,
					TokenType.MODERATE_PASTURE);
        fichas.add(deforest);
        Ficha reforest = new Ficha (0, 5, alice.getColor(),
				TokenType.MANAGED_FOREST);
        fichas.add(reforest);
        setIds (fichas.toArray(new Ficha[] {}));
        fichas.remove(deforest);
        fichas.remove(reforest);

        for (Ficha ficha : fichas) {
            game.getGrid().updateCell(ficha);
        }

		ManantialesMove move = new ManantialesMove ();
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
		bob.setTurn(false);
		alice.setTurn(true);		
		move = new ManantialesMove (alice, deforest, reforest);
		
		game.move (move);				
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (0, game.getCheckConditions().size());	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSouthernBorderRelief() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (4,6, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man2 = new Ficha (4,7, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (4,8, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha resolve = new Ficha (4,6, alice.getColor(),
				TokenType.MANAGED_FOREST);
        setIds (man1, man2, man3, resolve);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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

		move = new ManantialesMove (alice, man3, resolve);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());			
		assertEquals (0, game.getCheckConditions().size());		
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testNorthernBorderRelief() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (4,0, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man2 = new Ficha (4,1, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (4,2, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha resolve = new Ficha (4,0, alice.getColor(),
				TokenType.MANAGED_FOREST);
        setIds (man1, man2, man3, resolve);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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

		move = new ManantialesMove (alice, man3, resolve);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());	
		assertEquals (0, game.getCheckConditions().size());			
		
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEasternBorderRelief() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (6,4, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man2 = new Ficha (7,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (8,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha resolve = new Ficha (6,4, alice.getColor(),
				TokenType.MANAGED_FOREST);
        setIds (man1, man2, man3, resolve);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		

		move = new ManantialesMove (alice, resolve);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());	
		assertEquals (0, game.getCheckConditions().size());		
		
	}
	
	@SuppressWarnings("unchecked")
	@Test	
	public void testWesternBorderRelief() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (2,4, alice.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man2 = new Ficha (1,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (0,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
        Ficha resolve = new Ficha (2,4, alice.getColor(),
				TokenType.MANAGED_FOREST);
        setIds (man1, man2, man3, resolve);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		

		move = new ManantialesMove (alice, resolve);
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());	
		assertEquals (0, game.getCheckConditions().size());				
	}
	
	@SuppressWarnings("unchecked")
	@Test	
	public void testWesternBorderExpiration() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (2,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (1,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (0,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
        Ficha terminator = new Ficha (0,2, bob.getColor(),
				TokenType.MANAGED_FOREST);
        setIds (man1, man2, man3, terminator);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		

		move = new ManantialesMove (bob, terminator);
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
            Ficha ficha = (Ficha) cell;
            if (ficha.getBorder().equals(BorderType.WEST))
                filter.add(ficha);
        }

        assertTrue (filter.size() == 0);
		
	}

	@SuppressWarnings("unchecked")
	@Test	
	public void testEasternBorderExpiration() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (6,4, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (7,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (8,4, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);		
        Ficha terminator = new Ficha (0,2, bob.getColor(),
				TokenType.MANAGED_FOREST);
        setIds (man1,man2,man3,terminator);

        game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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

		move = new ManantialesMove (bob, terminator);
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
            Ficha ficha = (Ficha) cell;
            if (ficha.getBorder().equals(BorderType.EAST))
                filter.add(ficha);
        }

        assertTrue (filter.size() == 0);
		
	}	

	@SuppressWarnings("unchecked")
	@Test	
	public void testSouthernBorderExpiration() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (4,6, alice.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (4,7, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (4,8, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha terminator = new Ficha (0,2, bob.getColor(),
				TokenType.MANAGED_FOREST);
        setIds (man1,man2,man3,terminator);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		move = new ManantialesMove (bob, terminator);
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
            Ficha ficha = (Ficha) cell;
            if (ficha.getBorder().equals(BorderType.SOUTH))
                filter.add(ficha);
        }

        assertTrue (filter.size() == 0);
		
	}	

	@SuppressWarnings("unchecked")
	@Test	
	public void testNorthernBorderExpiration() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (4,0, charlie.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man2 = new Ficha (4,1, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
		Ficha man3 = new Ficha (4,2, charlie.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha terminator = new Ficha (0,2, bob.getColor(),
				TokenType.MANAGED_FOREST);
        setIds (man1,man2,man3,terminator);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		charlie.setTurn(true);
		ManantialesMove move = new ManantialesMove (charlie, man3);
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
		move = new ManantialesMove (bob, terminator);
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
            Ficha ficha = (Ficha) cell;
            if (ficha.getBorder().equals(BorderType.NORTH))
                filter.add(ficha);
        }

        assertTrue (filter.size() == 0);
		
	}

    @SuppressWarnings("unchecked")
	@Test
	public void testWin () throws JMSException, InvalidMoveException {
		
		game.setMode(Mode.SILVOPASTORAL);

		Point [] points = { new Point (0,8), new Point (0,6), new Point (0,4),
				new Point (1,4), new Point (2,4), new Point (3,4), 
				new Point (1,5), new Point (1,7) };

        LinkedList<Ficha> fichas = new LinkedList<Ficha>();
		for (int i = 0; i < 8; i++) {			
			Ficha ficha = null;
			ficha = new Ficha (points [ i ].x, points [ i ].y, Color.RED,
						TokenType.SILVOPASTORAL);					
			denise.setSilvo(denise.getSilvo() + 1);						
			fichas.add(ficha);
		}
		Ficha end = new Ficha (3,7, Color.RED,
				TokenType.MANAGED_FOREST);
        fichas.add(end);
        setIds (fichas.toArray(new Ficha[] {}));
        for (Ficha ficha : fichas) {
            game.getGrid().updateCell(ficha);
        }

		/* Denise is RED */
		denise.setTurn(true);
		ManantialesMove move = new ManantialesMove (denise, end);
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());				
		assertEquals (33, denise.getScore());
		
		ArrayList filter = new ArrayList();		
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("END"))
					filter.add(message);
		}
		
		assertTrue (filter.size() > 0);	
	}
	
	/*
	 * 
	 */
    @SuppressWarnings("unchecked")
	@Test 
	public void testReplaceModerateWithIntensiveOnManantial() throws JMSException, InvalidMoveException {
		game.setState(GameState.PLAY);
		
		Ficha man1 = new Ficha (4,3, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man2 = new Ficha (4,5, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man3 = new Ficha (4,5, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
        setIds (man1, man2, man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		bob.setTurn(true);		
		ManantialesMove move = new ManantialesMove ();
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
		
		Ficha man1 = new Ficha (0,4, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man2 = new Ficha (1,4, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man3 = new Ficha (0,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
        setIds (man1, man2, man3);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		bob.setTurn(true);		
		ManantialesMove move = new ManantialesMove (bob, man1, man3);
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
		
		Ficha man1 = new Ficha (0,4, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man2 = new Ficha (1,4, bob.getColor(), 
				TokenType.MODERATE_PASTURE);
		Ficha man3 = new Ficha (0,4, bob.getColor(), 
				TokenType.INTENSIVE_PASTURE);
        Ficha man4 = new Ficha (4,0, alice.getColor(), TokenType.MODERATE_PASTURE);
        Ficha man5 = new Ficha (4,1, alice.getColor(), TokenType.MODERATE_PASTURE);
        Ficha man6 = new Ficha (8,4, charlie.getColor(), TokenType.MODERATE_PASTURE);
        Ficha man7 = new Ficha (7,4, charlie.getColor(), TokenType.MODERATE_PASTURE);
        setIds (man1, man2, man3, man4, man5, man6, man7);

		game.getGrid().updateCell(man1);
		game.getGrid().updateCell(man2);
		
		/* Set MODERATES on the other borders */
		game.getGrid().updateCell (man4);
		game.getGrid().updateCell(man5);
		game.getGrid().updateCell(man6);
		game.getGrid().updateCell(man7);
		
		/* Convert Moderate to Intensive */
		bob.setTurn(true);		
		ManantialesMove move = new ManantialesMove (bob, man1, man3);
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
