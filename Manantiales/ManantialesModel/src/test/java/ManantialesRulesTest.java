/*
* Copyright (C) 2010-2012 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

import java.util.*;

import javax.jms.JMSException;
import javax.jms.Message;


import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.mock.jms.MockObjectMessage;
import com.mockrunner.mock.jms.MockTopic;
import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.entity.*;

import mx.ecosur.multigame.manantiales.entity.*;

import mx.ecosur.multigame.manantiales.enums.BorderType;
import mx.ecosur.multigame.manantiales.enums.ConditionType;
import mx.ecosur.multigame.manantiales.enums.TokenType;
import mx.ecosur.multigame.manantiales.util.RuleFunctions;

import mx.ecosur.multigame.grid.entity.GameGrid;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.grid.entity.GridRegistrant;

import static util.TestUtilities.*;
import static mx.ecosur.multigame.manantiales.util.RuleFunctions.*;

import org.junit.Before;
import org.junit.Test;

public class ManantialesRulesTest extends JMSTestCaseAdapter {
        
    private ManantialesGame game;
        
    private ManantialesPlayer alice, bob, charlie, denise;

    private MockTopic mockTopic;

    private EJBTestModule ejbModule;    

    @Before
    public void setUp() throws Exception {
        super.setUp();

        /* Set up mock JMS destination for message sender */
        ejbModule = createEJBTestModule();
        ejbModule.bindToContext("MultiGameConnectionFactory",
                getJMSMockObjectFactory().getMockTopicConnectionFactory());
        mockTopic = getDestinationManager().createTopic("MultiGame");
        ejbModule.bindToContext("MultiGame", mockTopic);

        game = new ManantialesGame();
        GridRegistrant[] players = {
            new GridRegistrant ("alice"),
            new GridRegistrant ("bob"),
            new GridRegistrant ("charlie"),
            new GridRegistrant ("denise") };

        int counter = 0;
        for (int i = 0; i < players.length; i++) {
            game.registerPlayer (players [ counter++ ]);
        }

        for (GridPlayer player : game.getPlayers()) {
            if (player.getName().equals("alice")) {
                    alice = (ManantialesPlayer) player;
            } else if (player.getName().equals("bob")) {
                    bob = (ManantialesPlayer) player;
            } else if (player.getName().equals("charlie")) {
                    charlie = (ManantialesPlayer) player;
            } else if (player.getName().equals("denise")) {
                    denise = (ManantialesPlayer)player;
            }
        }
    }


        
    @Test
    public void testInitialize () {
        assertTrue (game.getGrid().isEmpty());
        Collection<GridPlayer> players = game.getPlayers();
        GridPlayer p = null;
        for (GridPlayer player : players) {
                if (player.getName().equals("alice")) {
                    p = player;
                    assertTrue(player.getColor().equals(Color.YELLOW));
                } else if (player.getName().equals("bob"))
                    assertTrue(player.getColor().equals(Color.PURPLE));
                else if (player.getName().equals("charlie"))
                    assertTrue(player.getColor().equals(Color.RED));
                else if (player.getName().equals("denise"))
                    assertTrue(player.getColor().equals(Color.BLACK));
        }
        assertNotNull (p);
        assertEquals ("alice", p.getName());
        assertEquals (true, p.isTurn());
    }
        
    @Test
    public void testExecuteMove () throws InvalidMoveException {
        ManantialesFicha play = new ManantialesFicha(1, 1, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        SetIds(play);
        ManantialesMove move = new ManantialesMove (alice, play);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (play, game.getGrid().getLocation(play));
        assertEquals (1, alice.getModerate());
        assertEquals (2, alice.getScore());
    }
        
    @Test
    public void testIntensiveMove () throws InvalidMoveException {
        ManantialesFicha mod = new ManantialesFicha(5, 4, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha intensive = new ManantialesFicha(5,4, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        SetIds(mod, intensive);

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
        ManantialesFicha contig1 = new ManantialesFicha(5, 4, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha contig2 = new ManantialesFicha(6, 4, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        SetIds(contig1, contig2);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>());
        game.getGrid().getCells().add(contig1);
        ManantialesMove move = new ManantialesMove (alice, contig2);
        game.move (move);
        assertEquals (MoveStatus.INVALID, move.getStatus());
    }
        
    @Test
    public void testColumnContiguousIntensiveConstraint () throws InvalidMoveException {
        ManantialesFicha contig1 = new ManantialesFicha(5, 4, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha contig2 = new ManantialesFicha(5, 5, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        SetIds(contig1, contig2);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>());
        game.getGrid().getCells().add(contig1);
        ManantialesMove move = new ManantialesMove (alice, contig2);
        game.move (move);
        assertEquals (MoveStatus.INVALID, move.getStatus());
    }


    @Test
    public void testDiagonalContiguousIntensiveConstraint() throws InvalidMoveException {
        ManantialesFicha contig1 = new ManantialesFicha(6, 4, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha contig2 = new ManantialesFicha(5, 5, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        SetIds(contig1, contig2);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>());
        game.getGrid().getCells().add(contig1);
        ManantialesMove move = new ManantialesMove (alice, contig2);
        game.move (move);
        assertEquals (MoveStatus.INVALID, move.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testManantialesCheckConstraint () throws JMSException, InvalidMoveException {
        ManantialesFicha man1 = new ManantialesFicha(4,3, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,5, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(3,4, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        SetIds(man1, man2, man3);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells( new TreeSet<GridCell>(new CellComparator()) );
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

        alice.setTurn(false);
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
        ManantialesFicha man1 = new ManantialesFicha(4,3, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,5, bob.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(3,4, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        SetIds(man1, man2, man3);
        game.getGrid().setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);
        ManantialesMove move = new ManantialesMove (charlie, man3);
        game.move (move);
        assertTrue(game.getCheckConditions().size() == 1);
        ManantialesFicha terminator = new ManantialesFicha(1,4, bob.getColor(), TokenType.MANAGED_FOREST);
        bob.setTurn(true);
        move = new ManantialesMove (bob, terminator);
        game.move (move);
        assertEquals (GameState.ENDED, game.getState());
        ArrayList filter = new ArrayList();
        List<Message> messageList = mockTopic.getReceivedMessageList();
        for (Message  message : messageList) {
                if (message.getStringProperty("GAME_EVENT").equals("END")) {
                    filter.add(message);
                    break;
                }
        }
        assertTrue (filter.size() > 0);
        filter.clear();
        messageList = mockTopic.getReceivedMessageList();
        for (Message  message : messageList) {
                if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED")) {
                    filter.add(message);
                    break;
                }
        }
        assertTrue (filter.size() > 0);
        filter.clear();
        for (CheckCondition constraint : game.getCheckConditions()) {
                if (constraint.isExpired())
                        filter.add(constraint);
        }
        assertTrue (filter.size() > 0);
    }

    @Test
    public void testManantialesCheckConstraintRelief() throws InvalidMoveException, 
        JMSException 
    {
        ManantialesFicha man1 = new ManantialesFicha(4,3, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,5, bob.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(3,4, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man4 = new ManantialesFicha(5,5, denise.getColor(),
                        TokenType.MANAGED_FOREST);
        ManantialesFicha resolver = new ManantialesFicha(4,3, alice.getColor(),
                TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, man4, resolver);
        game.getGrid().setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);
        charlie.setTurn(true);
        ManantialesMove move = new ManantialesMove (charlie, man3);
        game.move (move);
        
        /* Unrelated move by Denise */
        move = new ManantialesMove(denise, man4);
        game.move(move);
        
        /* Fix the first condition and relieve the checkConstraint
         */
        move = new ManantialesMove (alice, man1, resolver);
        game.move (move);
        assertEquals(MoveStatus.EVALUATED, move.getStatus());
        assertEquals (0, game.getCheckConditions().size());
        ArrayList filter = new ArrayList();
        List<Message> messageList = mockTopic.getReceivedMessageList();
        for (Message  message : messageList) {
                if (message.getStringProperty("GAME_EVENT").equals(
                        "CONDITION_RESOLVED")) 
                {
                            filter.add(message);
                            break;
                }
        }
        assertEquals(1, filter.size());
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testSouthernBorderDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
        ManantialesFicha man1 = new ManantialesFicha(4,6, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,7, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(4,8, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        SetIds(man1, man2, man3);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);
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
        assertTrue ("No RAISED_CONDITION message intercepted!", filter.size() == 1);

    }
        
    @SuppressWarnings("unchecked")
    @Test
    public void testEasternBorderDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
        ManantialesFicha man1 = new ManantialesFicha(6,4, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(7,4, charlie.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(8,4, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        SetIds(man1, man2, man3);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);
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
        ManantialesFicha man1 = new ManantialesFicha(2,4, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(1,4, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(0,4, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        SetIds(man1, man2, man3);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);
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
        assertTrue ("No RAISED_CONDITION message intercepted!",filter.size() == 1);
        assertTrue ("Filter.size()==" + filter.size(), filter.size() == 1);
    }
        
    @SuppressWarnings("unchecked")
    @Test
    public void testNorthernBorderDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
        ManantialesFicha man1 = new ManantialesFicha(4,0, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,1, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(4,2, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        SetIds(man1, man2, man3);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);
        alice.setTurn(false);
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
        assertTrue ("No RAISED_CONDITION message intercepted!", filter.size() == 1);
        assertTrue ("Filter.size()==" + filter.size(), filter.size() == 1);

    }

    @Test
    public void testBadYear () throws InvalidMoveException {

        ManantialesFicha ficha = new ManantialesFicha(4,0, alice.getColor(), TokenType.MANAGED_FOREST);
        ManantialesMove move = new ManantialesMove ();
        move.setPlayer(alice);
        move.setDestinationCell(ficha);
        move.setBadYear(true);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
    }
        
        
    @SuppressWarnings("unchecked")
    @Test
    public void testDeforestedCheckConstraint () throws JMSException, InvalidMoveException {
        List<ManantialesFicha> fichas = new LinkedList<ManantialesFicha>();
        /* Populate board with MODERATE tokens */
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                /* skip the last ficha */
                if (i == 7 && j==3)
                    continue;
                ManantialesFicha ficha = new ManantialesFicha(i, j, Color.BLACK,
                    TokenType.MODERATE_PASTURE);
                fichas.add(ficha);
            }
        }
        ManantialesFicha deforest = new ManantialesFicha(0,5, Color.RED,
                        TokenType.MODERATE_PASTURE);
        fichas.add(deforest);
        ManantialesFicha[] fichaArr = fichas.toArray(new ManantialesFicha[] {});
        SetIds(fichaArr);
        fichas.remove(deforest);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));

        for (ManantialesFicha ficha : fichas)
            game.getGrid().getCells().add(ficha);
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
        List<ManantialesFicha> fichas = new LinkedList<ManantialesFicha>();

        /* Populate board with MODERATE tokens */
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                /* skip the last ficha */
                if (i == 7 && j==3)
                    continue;
                ManantialesFicha ficha = new ManantialesFicha(i, j, Color.BLACK,
                    TokenType.MODERATE_PASTURE);
                fichas.add(ficha);
            }
        }

        ManantialesFicha deforest = new ManantialesFicha(0,5, Color.RED,
            TokenType.MODERATE_PASTURE);
        fichas.add(deforest);
        SetIds(fichas.toArray(new ManantialesFicha[]{}));
        fichas.remove(deforest);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));

        for (ManantialesFicha ficha : fichas) {
            game.getGrid().getCells().add(ficha);
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
        denise.setTurn (true);
        ManantialesFicha terminator = new ManantialesFicha(0,6, denise.getColor(),
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
        List<ManantialesFicha> fichas = new LinkedList<ManantialesFicha>();

        /* Populate board with MODERATE tokens */
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                /* skip the last ficha */
                if (i == 7 && j==3)
                    continue;
                ManantialesFicha ficha = new ManantialesFicha(i, j, Color.BLACK,
                    TokenType.MODERATE_PASTURE);
                fichas.add(ficha);
            }
        }

        ManantialesFicha deforest = new ManantialesFicha(0,5, alice.getColor(),
                                TokenType.MODERATE_PASTURE);
        fichas.add(deforest);
        ManantialesFicha reforest = new ManantialesFicha(0, 5, alice.getColor(),
                                TokenType.MANAGED_FOREST);
        fichas.add(reforest);
        SetIds(fichas.toArray(new ManantialesFicha[]{}));
        fichas.remove(deforest);
        fichas.remove(reforest);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));


        for (ManantialesFicha ficha : fichas) {
            game.getGrid().getCells().add(ficha);
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
        ManantialesFicha man1 = new ManantialesFicha(4,6, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,7, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(4,8, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha resolve = new ManantialesFicha(4,6, alice.getColor(),
                        TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, resolve);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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

        ManantialesFicha man1 = new ManantialesFicha(4,0, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,1, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(4,2, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha resolve = new ManantialesFicha(4,0, alice.getColor(),
                        TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, resolve);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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
        alice.setTurn (true);
        move = new ManantialesMove (alice, man3, resolve);
        game.move (move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (0, game.getCheckConditions().size());


    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEasternBorderRelief() throws JMSException, InvalidMoveException {
        ManantialesFicha man1 = new ManantialesFicha(6,4, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(7,4, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(8,4, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha resolve = new ManantialesFicha(6,4, alice.getColor(),
                        TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, resolve);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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
        ManantialesFicha man1 = new ManantialesFicha(2,4, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(1,4, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(0,4, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha resolve = new ManantialesFicha(2,4, alice.getColor(),
                        TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, resolve);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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
        ManantialesFicha man1 = new ManantialesFicha(2,4, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(1,4, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(0,4, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha terminator = new ManantialesFicha(0,2, bob.getColor(),
                        TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, terminator);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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

        /* Now have the previous player move */
        bob.setTurn (true);


        move = new ManantialesMove (bob, terminator);
        game.move (move);

        filter.clear();
        messageList = mockTopic.getReceivedMessageList();
        for (Message  message : messageList) {
                if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
                                filter.add(message);
        }

        assertTrue (filter.size() > 0);
        
        assertTrue (isTerritoryCleared (BorderType.WEST, game.getGrid()));        

        filter.clear();

        /* Ensure that there are no MODERATE or INTENSIVE fichas on the Border */
        for (GridCell cell : game.getGrid().getCells()) {
        ManantialesFicha ficha = (ManantialesFicha) cell;
        if (ficha.getBorder().equals(BorderType.WEST))
            filter.add(ficha);
        }

        assertTrue (filter.size() == 0);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEasternBorderExpiration() throws JMSException, InvalidMoveException {
        ManantialesFicha man1 = new ManantialesFicha(6,4, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(7,4, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(8,4, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha terminator = new ManantialesFicha(0,2, bob.getColor(),
                        TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, terminator);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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
        ManantialesFicha ficha = (ManantialesFicha) cell;
        if (ficha.getBorder().equals(BorderType.EAST))
            filter.add(ficha);
        }

        assertTrue (filter.size() == 0);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSouthernBorderExpiration() throws JMSException, InvalidMoveException {
        ManantialesFicha man1 = new ManantialesFicha(4,6, alice.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,7, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(4,8, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha terminator = new ManantialesFicha(0,2, bob.getColor(),
                        TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, terminator);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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
        ManantialesFicha ficha = (ManantialesFicha) cell;
        if (ficha.getBorder().equals(BorderType.SOUTH))
            filter.add(ficha);
        }

        assertTrue (filter.size() == 0);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNorthernBorderExpiration() throws JMSException, InvalidMoveException {
        ManantialesFicha man1 = new ManantialesFicha(4,0, charlie.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,1, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(4,2, charlie.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha terminator = new ManantialesFicha(0,2, bob.getColor(),
                        TokenType.MANAGED_FOREST);
        SetIds(man1, man2, man3, terminator);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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


        assertTrue ("No conditions triggered!", filter.size() > 0);

        filter.clear();

        /* Ensure that there are no MODERATE or INTENSIVE fichas on the Border */
        for (GridCell cell : game.getGrid().getCells()) {
        ManantialesFicha ficha = (ManantialesFicha) cell;
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
        ManantialesFicha man1 = new ManantialesFicha(4,3, bob.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,5, bob.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(4,5, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        SetIds(man1, man2, man3);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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
        ManantialesFicha man1 = new ManantialesFicha(0,4, bob.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(1,4, bob.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(0,4, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        SetIds(man1, man2, man3);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

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
        ManantialesFicha man1 = new ManantialesFicha(0,4, bob.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(1,4, bob.getColor(),
                        TokenType.MODERATE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(0,4, bob.getColor(),
                        TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man4 = new ManantialesFicha(4,0, alice.getColor(), TokenType.MODERATE_PASTURE);
        ManantialesFicha man5 = new ManantialesFicha(4,1, alice.getColor(), TokenType.MODERATE_PASTURE);
        ManantialesFicha man6 = new ManantialesFicha(8,4, charlie.getColor(), TokenType.MODERATE_PASTURE);
        ManantialesFicha man7 = new ManantialesFicha(7,4, charlie.getColor(), TokenType.MODERATE_PASTURE);
        SetIds(man1, man2, man3, man4, man5, man6, man7);

        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);

        /* Set MODERATES on the other borders */
        game.getGrid().getCells().add(man4);
        game.getGrid().getCells().add(man5);
        game.getGrid().getCells().add(man6);
        game.getGrid().getCells().add(man7);

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

    @SuppressWarnings("unchecked")
    @Test
    public void testSuggestionAccepted () throws InvalidMoveException, JMSException {
        ManantialesFicha play = new ManantialesFicha(5, 4, alice.getColor(), TokenType.MODERATE_PASTURE);
        ManantialesFicha moved = new ManantialesFicha(4, 0, alice.getColor(), TokenType.MODERATE_PASTURE);
        SetIds(play, moved);
        ManantialesMove move = new ManantialesMove (alice, play);
        move = (ManantialesMove) game.move (move);
        assertEquals(MoveStatus.EVALUATED, move.getStatus());
        mockTopic.clear();
        PuzzleSuggestion suggestion = new PuzzleSuggestion();
        suggestion.setSuggestor(bob);
        suggestion.setStatus(SuggestionStatus.UNEVALUATED);
        move = new ManantialesMove (alice, play, moved);
        suggestion.setMove (move);
        suggestion = (PuzzleSuggestion) game.suggest(suggestion);
        assertTrue (suggestion.getStatus() == SuggestionStatus.EVALUATED);
        assertTrue (move.getStatus () == MoveStatus.UNVERIFIED);
        suggestion.setStatus(SuggestionStatus.ACCEPT);
        suggestion = (PuzzleSuggestion) game.suggest (suggestion);
        assertEquals(MoveStatus.EVALUATED,suggestion.getMove().getStatus());
        ArrayList filter = new ArrayList();
        List<Message> messageList = mockTopic.getReceivedMessageList();
        for (Message  message : messageList) {
            if (message.getStringProperty("GAME_EVENT").equals("MOVE_COMPLETE")) {
                MockObjectMessage objMessage = (MockObjectMessage) message;
                ManantialesMove temp = (ManantialesMove) objMessage.getObject();
                if (temp.getDestinationCell().equals(moved)) {
                    move = temp;
                    filter.add(message);
                    break;
                }
            }
        }

        assertTrue ("Move not evaluted.  Status [" + move.getStatus() + "]", move.getStatus().equals(
                MoveStatus.EVALUATED));
        assertTrue(filter.size() > 0);
        GameGrid grid = game.getGrid();
        GridCell location =  grid.getLocation(move.getDestinationCell());
        assertTrue ("Destination not populated!", location != null);
        assertTrue ("Destination not populated!", location.equals(moved));        
        assertTrue ("Location remains!", grid.getLocation(move.getCurrentCell()) == null);
        assertTrue ("Gamegrid is contains both tokens!", grid.getCells().size() == 1);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testSuggestionRejected () throws InvalidMoveException, JMSException {
        ManantialesFicha play = new ManantialesFicha(5, 4, alice.getColor(), TokenType.MODERATE_PASTURE);
        SetIds(play);
        ManantialesMove move = new ManantialesMove (alice, play);
        game.move (move);

        PuzzleSuggestion suggestion = new PuzzleSuggestion();
        suggestion.setSuggestor(bob);
        suggestion.setStatus(SuggestionStatus.UNEVALUATED);
        ManantialesFicha change = new ManantialesFicha(4, 0, alice.getColor(), TokenType.MODERATE_PASTURE);
        move = new ManantialesMove (alice, play, change);
        suggestion.setMove (move);
        mockTopic.clear();
        suggestion = (PuzzleSuggestion) game.suggest(suggestion);

        assertTrue (suggestion.getStatus() == SuggestionStatus.EVALUATED);

        suggestion.setStatus(SuggestionStatus.REJECT);
        suggestion = (PuzzleSuggestion) game.suggest(suggestion);
        
        ArrayList filter = new ArrayList();
        List<Message> messageList = mockTopic.getReceivedMessageList();
        for (Message  message : messageList) {
            if (message.getStringProperty("GAME_EVENT").equals("MOVE_COMPLETE")) {
                MockObjectMessage objMessage = (MockObjectMessage) message;
                ManantialesMove temp = (ManantialesMove) objMessage.getObject();
                if (temp.getDestinationCell().equals(change)) {
                    filter.add(message);
                    move = temp;
                }
            }
        }

        assertTrue (filter.size() == 0);
        assertTrue (move.getStatus().equals(MoveStatus.UNVERIFIED));

        GameGrid grid = game.getGrid();
        GridCell location =  grid.getLocation(move.getDestinationCell());
        assertTrue ("Destination populated!", location == null);
        assertTrue ("Location does not remain!", grid.getLocation(move.getCurrentCell()) != null);
    }

    @Test
    public void testSwapSuggestionAccepted () throws InvalidMoveException, JMSException {
        ManantialesFicha play = new ManantialesFicha (5, 4, alice.getColor(), TokenType.INTENSIVE_PASTURE);
        ManantialesFicha change = new ManantialesFicha (4, 0, alice.getColor(), TokenType.MODERATE_PASTURE);

        SetIds(play, change);
        ManantialesMove move = new ManantialesMove (alice, play);
        game.move (move);

        mockTopic.clear();

        PuzzleSuggestion suggestion = new PuzzleSuggestion();
        suggestion.setSuggestor(bob);
        suggestion.setStatus(SuggestionStatus.UNEVALUATED);
        move = new ManantialesMove (alice, play, change, true);
        suggestion.setMove (move);
        suggestion = (PuzzleSuggestion) game.suggest(suggestion);

        assertTrue (suggestion.getStatus() == SuggestionStatus.EVALUATED);
        assertTrue (move.getStatus () == MoveStatus.UNVERIFIED);

        suggestion.setStatus(SuggestionStatus.ACCEPT);
        suggestion = (PuzzleSuggestion) game.suggest (suggestion);

        GameGrid grid = game.getGrid();
        ManantialesFicha location1 =  (ManantialesFicha) grid.getLocation(change);
        assertTrue ("Destination not populated!", location1 != null);
        ManantialesFicha location2 =  (ManantialesFicha) grid.getLocation(play);
        assertTrue ("Destination not populated!", location2 != null);
    }
    
    @Test
    public void testIsPrecedingPlayer () {
        assertTrue("Next player is: " + RuleFunctions.nextPlayer(game, alice), RuleFunctions.isPrecedingPlayer(game, alice, bob));
        assertTrue("Next player is: " + RuleFunctions.nextPlayer(game, bob), RuleFunctions.isPrecedingPlayer(game, bob, charlie));
        assertTrue("Next player is: " + RuleFunctions.nextPlayer(game, charlie), RuleFunctions.isPrecedingPlayer(game, charlie, denise));
        assertTrue("Next player is: " + RuleFunctions.nextPlayer(game, denise), RuleFunctions.isPrecedingPlayer(game, denise, alice));
    }
    
    @Test
    public void testRowContiguousIntensives() throws InvalidMoveException {
        ManantialesFicha a1 = new ManantialesFicha (4,0,alice.getColor(), 
                TokenType.MODERATE_PASTURE);
        ManantialesFicha a2 = new ManantialesFicha (4,0, alice.getColor(),
                TokenType.INTENSIVE_PASTURE);
        ManantialesFicha b1 = new ManantialesFicha (4,1, alice.getColor(),
                TokenType.MODERATE_PASTURE);
        ManantialesFicha b2 = new ManantialesFicha (4,1, alice.getColor(),
                TokenType.INTENSIVE_PASTURE);
        InvalidMoveException exception = null;
        SetIds(a1,b1,a2,b2);
        ManantialesMove m = new ManantialesMove(alice, a1);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice, b1);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice,a1,a2);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice,b1,b2);
        game.move(m);
        assertTrue("Unexpected Status! " + m.getStatus().name(),
                m.getStatus().equals(MoveStatus.INVALID));
    }
    
    public void testColumnContiguousIntensives() throws InvalidMoveException {
        ManantialesFicha a1 = new ManantialesFicha (3,1,alice.getColor(), 
                TokenType.MODERATE_PASTURE);
        ManantialesFicha a2 = new ManantialesFicha (3,1, alice.getColor(),
                TokenType.INTENSIVE_PASTURE);
        ManantialesFicha b1 = new ManantialesFicha (4,1, alice.getColor(),
                TokenType.MODERATE_PASTURE);
        ManantialesFicha b2 = new ManantialesFicha (4,1, alice.getColor(),
                TokenType.INTENSIVE_PASTURE);
        InvalidMoveException exception = null;
        SetIds(a1,b1,a2,b2);
        ManantialesMove m = new ManantialesMove(alice, a1);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice, b1);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice,a1,a2);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice,b1,b2);
        game.move(m);
        assertTrue("Unexpected Status! " + m.getStatus().name(),
                m.getStatus().equals(MoveStatus.INVALID));
    }
    
    public void testNotConnectedRowContiguousIntensiveS() throws InvalidMoveException {
        ManantialesFicha a1 = new ManantialesFicha (2,2,alice.getColor(), 
                TokenType.MODERATE_PASTURE);
        ManantialesFicha a2 = new ManantialesFicha (2,2, alice.getColor(),
                TokenType.INTENSIVE_PASTURE);
        ManantialesFicha b1 = new ManantialesFicha (4,2, alice.getColor(),
                TokenType.MODERATE_PASTURE);
        ManantialesFicha b2 = new ManantialesFicha (4,2, alice.getColor(),
                TokenType.INTENSIVE_PASTURE);
        InvalidMoveException exception = null;
        SetIds(a1,b1,a2,b2);
        ManantialesMove m = new ManantialesMove(alice, a1);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice, b1);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice,a1,a2);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice,b1,b2);
        game.move(m);
        assertTrue("Unexpected Status! " + m.getStatus().name(), 
                m.getStatus().equals(MoveStatus.EVALUATED));
    }
    
    @Test
    public void testBorderColumnSeperatedIntensives () throws InvalidMoveException {
        ManantialesFicha a1 = new ManantialesFicha (3,1,alice.getColor(), 
                TokenType.MODERATE_PASTURE);
        ManantialesFicha a2 = new ManantialesFicha (3,1, alice.getColor(),
                TokenType.INTENSIVE_PASTURE);
        ManantialesFicha b1 = new ManantialesFicha (5,1, bob.getColor(),
                TokenType.MODERATE_PASTURE);
        ManantialesFicha b2 = new ManantialesFicha (5,1, bob.getColor(),
                TokenType.INTENSIVE_PASTURE);
        SetIds (a1,b1,a2,b2);
        ManantialesMove m = new ManantialesMove (alice, a1);
        game.move (m);        
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(bob, b1);
        game.move (m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        alice.setTurn(true);
        m = new ManantialesMove (alice, a1, a2);
        game.move (m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(bob, b1, b2);
        game.move (m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
    }
   
    @Test
    public void testBorderRowSeperatedIntensives() throws InvalidMoveException {
        ManantialesFicha a1 = new ManantialesFicha (3,3, alice.getColor(), 
                TokenType.MODERATE_PASTURE);
        ManantialesFicha c1 = new ManantialesFicha (3,5, charlie.getColor(),
                TokenType.MODERATE_PASTURE);
        ManantialesFicha a2 = new ManantialesFicha (3,3, alice.getColor(), 
                TokenType.INTENSIVE_PASTURE);
        ManantialesFicha c2 = new ManantialesFicha (3,5, charlie.getColor(),
                TokenType.INTENSIVE_PASTURE);
        SetIds (a1,c1,a2,c2);
        ManantialesMove m = new ManantialesMove (alice, a1);
        game.move (m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        charlie.setTurn(true);
        m = new ManantialesMove(charlie,c1);
        game.move (m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        alice.setTurn(true);
        m = new ManantialesMove (alice, a1, a2);
        game.move (m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        charlie.setTurn(true);
        m = new ManantialesMove(charlie, c1, c2);
        game.move (m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
    }

    /* Tests the "special cases" due to the boards underlying graph, in 2,2 -> 4,2, 4,2->6,2,
     * 2,6 -> 4,6 4,6->6,6.
     */
    @Test
    public void testLeftAdjacentIntensiveSpecialCases() throws InvalidMoveException {
        ManantialesFicha fichas [] = {
            new ManantialesFicha(2,2, Color.YELLOW, TokenType.MODERATE_PASTURE),  //0
            new ManantialesFicha(4,2, Color.YELLOW, TokenType.MODERATE_PASTURE),  //1
            new ManantialesFicha(6,2, Color.PURPLE, TokenType.MODERATE_PASTURE),  //2
            new ManantialesFicha(2,2, Color.YELLOW, TokenType.INTENSIVE_PASTURE), //3
            new ManantialesFicha(6,2, Color.PURPLE, TokenType.INTENSIVE_PASTURE), //4
            new ManantialesFicha(4,2, Color.YELLOW, TokenType.INTENSIVE_PASTURE), //5
        };

        SetIds(fichas);

        /* Left side */
        ManantialesMove m = new ManantialesMove(alice, fichas [ 0 ]);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice, fichas [ 1 ]);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(bob, fichas [ 2 ]);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice, fichas[0], fichas[3]);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(bob, fichas[2], fichas [4] );
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
        m = new ManantialesMove(alice, fichas [1], fichas [5]);
        game.move(m);
        assertTrue (m.getStatus().name(), m.getStatus().equals(MoveStatus.EVALUATED));
    }

    @Test
    public void testConditionsEffected () {
        CheckCondition cond;
        ManantialesMove m;
        
        /* Case 1 */
        cond = new CheckCondition();
        cond.setExpired(false);
        cond.setType(ConditionType.NORTHERN_BORDER_DEFORESTED);
        m = new ManantialesMove();
        m.setDestinationCell(new ManantialesFicha(4,0,Color.YELLOW, TokenType.MODERATE_PASTURE));
        assertTrue(effectsCondition (cond, m));
        
        /* Case 2 */
        cond = new CheckCondition();
        cond.setExpired(false);
        cond.setType(ConditionType.EASTERN_BORDER_DEFORESTED);
        m = new ManantialesMove();
        m.setDestinationCell(new ManantialesFicha(5,4,Color.RED, TokenType.MODERATE_PASTURE));
        assertTrue(effectsCondition (cond, m));
        
        /* Case 3 */
        cond = new CheckCondition();
        cond.setExpired(false);
        cond.setType(ConditionType.WESTERN_BORDER_DEFORESTED);
        m = new ManantialesMove();
        m.setDestinationCell(new ManantialesFicha(1,4,Color.YELLOW, TokenType.MODERATE_PASTURE));
        assertTrue(effectsCondition (cond, m));
        
        /* Case 4 */
        cond = new CheckCondition();
        cond.setExpired(false);
        cond.setType(ConditionType.SOUTHERN_BORDER_DEFORESTED);
        m = new ManantialesMove();
        m.setDestinationCell(new ManantialesFicha(4,5,Color.PURPLE, TokenType.MODERATE_PASTURE));
        assertTrue(effectsCondition (cond, m));        
        
        /* Case 5 */
        cond = new CheckCondition();
        cond.setExpired(false);
        cond.setType(ConditionType.MANANTIALES_DRY);
        m = new ManantialesMove();
        m.setDestinationCell(new ManantialesFicha(3,4,Color.YELLOW, TokenType.MODERATE_PASTURE));
        assertTrue(effectsCondition (cond, m));
    }
    
    @Test
    public void testMoveEffectsNorthernBorderCondition () throws InvalidMoveException, JMSException {
        ManantialesFicha man1 = new ManantialesFicha(4,0, alice.getColor(),
                TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man2 = new ManantialesFicha(4,1, bob.getColor(),
                TokenType.INTENSIVE_PASTURE);
        ManantialesFicha man3 = new ManantialesFicha(4,2, charlie.getColor(),
                TokenType.MODERATE_PASTURE);
        ManantialesFicha man4 = new ManantialesFicha(4,3, bob.getColor(),
                TokenType.MODERATE_PASTURE);
        SetIds(man1, man2, man3, man4);
        GameGrid grid = game.getGrid();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(man1);
        game.getGrid().getCells().add(man2);
        alice.setTurn(false);
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
        assertTrue ("No RAISED_CONDITION message intercepted!", filter.size() == 1);
        assertTrue ("Filter.size()==" + filter.size(), filter.size() == 1);
        
        /*  Trigger the condition with a pasture token */
        move = new ManantialesMove(bob, man4);
        bob.setTurn(true);
        game.move(move);
        
        assertEquals(MoveStatus.EVALUATED, move.getStatus());
        filter = new ArrayList();
        messageList = mockTopic.getReceivedMessageList();
        for (Message  message : messageList) {
            if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED"))
                filter.add(message);
        }
        assertTrue ("No TRIGGERED_CONDITION message intercepted!", filter.size() == 1);
        assertTrue ("Filter.size()==" + filter.size(), filter.size() == 1);
        assertTrue(game.getGrid().isEmpty());
    }
    
    @Test
    public void testBasicBonuses() throws InvalidMoveException {
        ManantialesFicha [] fichas = {
                new ManantialesFicha(5,1, Color.RED, TokenType.MANAGED_FOREST),   //0  
                new ManantialesFicha(5,3, Color.RED, TokenType.MANAGED_FOREST),   //1
                new ManantialesFicha(7,1, Color.RED, TokenType.MANAGED_FOREST),
                new ManantialesFicha(7,3, Color.RED, TokenType.MODERATE_PASTURE)
        };
        
        SetIds(fichas);
        game.getGrid().updateCell(fichas [ 0 ]);
        game.getGrid().updateCell(fichas [ 1 ]);
        charlie.setTurn(true);
        ManantialesMove move = new ManantialesMove(charlie, fichas[2]);
        game.move(move);
        assertEquals(MoveStatus.EVALUATED, move.getStatus());
        assertEquals(1, charlie.getPremiums());
        move = new ManantialesMove(charlie,fichas[3]);
        game.move(move);
        assertEquals(MoveStatus.EVALUATED, move.getStatus());
        assertEquals(1, charlie.getPremiums());
    }

    @Test
    public void testIncrementTurn() {
        ManantialesMove move = new ManantialesMove(alice, new ManantialesFicha (4,4, alice.getColor(), TokenType.UNDEVELOPED));
        assertEquals(bob,incrementTurn(game,move));
        move = new ManantialesMove(bob, new ManantialesFicha (4,4, bob.getColor(), TokenType.UNDEVELOPED));
        assertEquals(charlie, incrementTurn(game,move));
        move = new ManantialesMove(charlie, new ManantialesFicha (4,4, charlie.getColor(), TokenType.UNDEVELOPED));
        assertEquals(denise, incrementTurn(game,move));
        move = new ManantialesMove(denise, new ManantialesFicha (4,4, denise.getColor(), TokenType.UNDEVELOPED));
        assertEquals(bob, incrementTurn(game,move));
    }

    /**
    * @param borderType
    * @param grid
    */
    private boolean isTerritoryCleared(BorderType borderType, GameGrid grid) {
        boolean ret  = grid.getCells() != null && grid.getCells().size() > 0;
        for (GridCell cell : grid.getCells()) {
            ManantialesFicha ficha = (ManantialesFicha) cell;
            if (ficha.getBorder().equals(borderType)) {
                ret = false;
                break;
            }
        }
        return ret;
    }
}
