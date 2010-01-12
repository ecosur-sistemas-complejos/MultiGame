/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
import mx.ecosur.multigame.impl.enums.tablon.TokenType;
import mx.ecosur.multigame.test.RulesTestBase;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.io.ResourceFactory;
import org.drools.builder.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import mx.ecosur.multigame.impl.entity.tablon.TablonGame;
import mx.ecosur.multigame.impl.entity.tablon.TablonPlayer;
import mx.ecosur.multigame.impl.entity.tablon.TablonMove;
import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;

import javax.jms.Message;
import javax.jms.JMSException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class TablonRulesTest extends RulesTestBase {

    private static KnowledgeBase tablon;

    /* Setup gente kbase */
    static {
        tablon = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(TablonGame.class.getResourceAsStream (
            "/mx/ecosur/multigame/impl/tablon.drl")), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newInputStreamResource(TablonGame.class.getResourceAsStream (
            "/mx/ecosur/multigame/impl/ruleflow/tablon-flow.rf")), ResourceType.DRF);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (errors.size() == 0)
            tablon.addKnowledgePackages(kbuilder.getKnowledgePackages());
        else {
            for (KnowledgeBuilderError error : errors) {
                System.out.println(error);
            }

            throw new RuntimeException ("Unable to load rule base!");
        }
    }

    private TablonGame game;

    private TablonPlayer alice, bob, charlie, denise;

    @Before
	public void setUp() throws Exception {
		super.setUp();

        game = new TablonGame(26, 26, tablon);        

		GridRegistrant a, b, c, d;
		a = new GridRegistrant ("alice");
		b = new GridRegistrant ("bob");
		c = new GridRegistrant ("charlie");
		d = new GridRegistrant ("denise");

		alice = (TablonPlayer) game.registerPlayer(a);
		bob = (TablonPlayer) game.registerPlayer(b);
		charlie = (TablonPlayer) game.registerPlayer(c);
		denise = (TablonPlayer) game.registerPlayer(d);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

    @Test
	public void testInitialize () {
		assertTrue (game.getGrid().getCells().size() != 0);
		Collection<GridPlayer> players = game.getPlayers();
		TablonPlayer p = null;
		for (GridPlayer player : players) {
			if (player.getRegistrant().getName().equals("alice")) {
				p = (TablonPlayer) player;
				break;
			}
		}

		assertNotNull (p);
		assertEquals ("alice", p.getRegistrant().getName());
		assertEquals (true, p.isTurn());
	}

@Test
	public void testExecuteMove () throws InvalidMoveException {
		TablonFicha token = new TablonFicha(2, 10, alice.getColor(), TokenType.POTRERO);		
		TablonMove move = new TablonMove(alice, token);
		game.move (move);
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (token, game.getGrid().getLocation(token));
        assertTrue (alice.isTurn() == false);
	}

    @Test
    public void testSoilConsequence () throws InvalidMoveException, JMSException {
        TablonFicha token = new TablonFicha(2, 10, alice.getColor(), TokenType.POTRERO);
		TablonMove move = new TablonMove(alice, token);
		game.move (move);

        ArrayList<Message> filter = new ArrayList<Message>();
		List<Message> messageList = mockTopic.getReceivedMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}
        mockTopic.clear();
		assertTrue ("Unexpected event(s) interecepted! " + filter, filter.size() == 0);
        
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (token, game.getGrid().getLocation(token));
        token = new TablonFicha (4,10, bob.getColor(), TokenType.POTRERO);
        move = new TablonMove(bob, token);
		game.move (move);

        filter = new ArrayList<Message>();
		messageList = messageList = mockTopic.getCurrentMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}
        mockTopic.clear();
		assertTrue ("Unexpected event(s) interecepted! " + filter, filter.size() == 0); 

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (token, game.getGrid().getLocation(token));
        token = new TablonFicha (4,8, charlie.getColor(), TokenType.POTRERO);
        move = new TablonMove(charlie, token);
		game.move (move);

        filter = new ArrayList<Message>();
		messageList = messageList = mockTopic.getCurrentMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}
        mockTopic.clear();
		assertTrue ("Unexpected event(s) interecepted! " + filter, filter.size() == 0);


        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (token, game.getGrid().getLocation(token));
        token = new TablonFicha (2,8, denise.getColor(), TokenType.POTRERO);
        move = new TablonMove(denise, token);
		game.move (move);

        filter = new ArrayList<Message>();
		messageList = messageList = mockTopic.getCurrentMessageList();
		for (Message  message : messageList) {
			if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
					filter.add(message);
		}
        mockTopic.clear();
		assertTrue ("Filter size is " + filter.size(), filter.size() > 0);

		assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (token, game.getGrid().getLocation(token));

        /* Check for consequences *//*
        /* We should have lost the soil particle at 3,3 */
        GridCell cell = game.getGrid().getLocation(new TablonFicha (3,9, Color.UNKNOWN, TokenType.SOIL_PARTICLE)); 
        assertNull (game.getGrid().toString(), cell);
    }
}
