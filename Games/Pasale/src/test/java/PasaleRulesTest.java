/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * @author awaterma@ecosur.mx
 */
import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.mock.jms.MockTopic;
import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.grid.model.GridCell;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGame;
import mx.ecosur.multigame.impl.entity.pasale.PasaleMove;
import mx.ecosur.multigame.impl.entity.pasale.PasalePlayer;
import mx.ecosur.multigame.impl.enums.pasale.TokenType;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.io.ResourceFactory;
import org.junit.Before;
import org.junit.Test;
import mx.ecosur.multigame.impl.entity.pasale.PasaleFicha;
import mx.ecosur.multigame.grid.model.GridRegistrant;
import mx.ecosur.multigame.grid.model.GridPlayer;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;

import javax.jms.Message;
import javax.jms.JMSException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class PasaleRulesTest extends JMSTestCaseAdapter {

    protected EJBTestModule ejbModule;

    private MockTopic mockTopic;

    protected static KnowledgeBase pasale;

    /* Setup gente kbase */
    static {

    }

    private PasaleGame game;

    private PasalePlayer alice, bob, charlie, denise;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        /* Set up mock JMS destination for message sender */
        ejbModule = createEJBTestModule();
        ejbModule.bindToContext("ConnectionFactory",
                getJMSMockObjectFactory().getMockTopicConnectionFactory());
        mockTopic = getDestinationManager().createTopic("MultiGame");
        ejbModule.bindToContext("MultiGame", mockTopic);
        game = new PasaleGame(26, 26);
        game.setMessageSender(new MessageSender());

        GridRegistrant a, b, c, d;
        a = new GridRegistrant ("alice");
        b = new GridRegistrant ("bob");
        c = new GridRegistrant ("charlie");
        d = new GridRegistrant ("denise");

        alice = (PasalePlayer) game.registerPlayer(a);
        bob = (PasalePlayer) game.registerPlayer(b);
        charlie = (PasalePlayer) game.registerPlayer(c);
        denise = (PasalePlayer) game.registerPlayer(d);
    }

    @Test
    public void testInitialize () {
        assertTrue (game.getGrid().getCells().size() != 0);
        Collection<GridPlayer> players = game.getPlayers();
        PasalePlayer p = null;
        for (GridPlayer player : players) {
            if (player.getRegistrant().getName().equals("alice")) {
                p = (PasalePlayer) player;
                break;
            }
        }

        assertNotNull (p);
        assertEquals ("alice", p.getRegistrant().getName());
        assertEquals (true, p.isTurn());
    }

    @Test
    public void testExecuteMove () throws InvalidMoveException {
        PasaleFicha token = new PasaleFicha(2, 10, alice.getColor(), TokenType.POTRERO);
        PasaleMove move = new PasaleMove(alice, token);
        move = (PasaleMove) game.move (move);
        assertEquals(MoveStatus.EVALUATED, move.getStatus());
        PasaleFicha existing = (PasaleFicha) game.getGrid().getLocation(token);
        assertEquals("Token types do not match!", token.getType(), existing.getType());
        assertTrue("It's still Alice's turn!", !alice.isTurn());
    }

    @SuppressWarnings (value= "unchecked")
    @Test
    public void testSoilConsequence () throws InvalidMoveException, JMSException {
        PasaleFicha token = new PasaleFicha(2, 10, alice.getColor(), TokenType.POTRERO);
        PasaleMove move = new PasaleMove(alice, token);
        game.move (move);

        ArrayList<Message> filter = new ArrayList<Message>();
        List<Message> messageList = (List<Message>) mockTopic.getReceivedMessageList();
        for (Message  message : messageList) {
            if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
                    filter.add(message);
        }
        mockTopic.clear();
        assertTrue ("Unexpected event(s) interecpted! " + filter, filter.size() == 0);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        PasaleFicha existing = (PasaleFicha) game.getGrid().getLocation(token);
        assertEquals ("Token types do not match!", token.getType(), existing.getType());
        token = new PasaleFicha(4,10, bob.getColor(), TokenType.POTRERO);
        move = new PasaleMove(bob, token);
        game.move (move);

        filter = new ArrayList<Message>();

        messageList = mockTopic.getCurrentMessageList();
        for (Message  message : messageList) {
            if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
                    filter.add(message);
        }
        mockTopic.clear();
        assertTrue ("Unexpected event(s) interecepted! " + filter, filter.size() == 0);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (token, game.getGrid().getLocation(token));
        token = new PasaleFicha(4,8, charlie.getColor(), TokenType.POTRERO);
        move = new PasaleMove(charlie, token);
        game.move (move);

        filter = new ArrayList<Message>();
        messageList = mockTopic.getCurrentMessageList();
        for (Message  message : messageList) {
            if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
                    filter.add(message);
        }
        mockTopic.clear();
        assertTrue ("Unexpected event(s) interecepted! " + filter, filter.size() == 0);


        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (token, game.getGrid().getLocation(token));
        token = new PasaleFicha(2,8, denise.getColor(), TokenType.POTRERO);
        move = new PasaleMove(denise, token);
        game.move (move);

        filter = new ArrayList<Message>();
        messageList = mockTopic.getCurrentMessageList();
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
        GridCell cell = game.getGrid().getLocation(new PasaleFicha(3,9, Color.UNKNOWN, TokenType.SOIL_PARTICLE));
        assertNull (game.getGrid().toString(), cell);
    }
}
