/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.DummyMessageSender;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;
import mx.ecosur.multigame.impl.enums.gente.GenteStrategy;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.interfaces.Move;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.*;

/**
 *
 * Each type of agent plays out a full game against other agents of the same type.
 * This ensures that agents work properly in a full gaming context.
 *
 * Tests fail if an agent cannot move or any other exception is generated.
 *
 */
public class GenteAgentTest extends GenteTestBase {

    protected GridRegistrant a,b,c,d;

    protected GenteStrategyAgent alice, bob, charlie, denise;

    protected GenteGame game;

    private static final int Ticks = 500;

@Before
    public void setUp() throws Exception {
        game = new GenteGame(gente);
        game.setMessageSender(new DummyMessageSender());
        a = new GridRegistrant ("alice");
        b = new GridRegistrant ("bob");
        c = new GridRegistrant ("charlie");
        d = new GridRegistrant ("denise");
    }


    @Test
    public void testBlockerAgent () throws Exception {
        int counter = Ticks;
        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.BLOCKER);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.BLOCKER);
        charlie = new GenteStrategyAgent (c, Color.RED, GenteStrategy.BLOCKER);
        denise = new GenteStrategyAgent (d, Color.GREEN, GenteStrategy.BLOCKER);

        game.registerAgent(alice);
        game.registerAgent(bob);
        game.registerAgent(charlie);
        game.registerAgent(denise);

        while (game.getState().equals(GameState.PLAY) && counter > 0) {
            boolean moved = false;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    GenteStrategyAgent agent = (GenteStrategyAgent) player;
                    Set<Move> moves = agent.determineMoves(game);
                    for (Move move : moves) {
                        game.move(move);
                        assertEquals (MoveStatus.EVALUATED, move.getStatus());
                        moved = true;
                        break;
                    }
                 }
                 counter--;
            }

            if (!moved)
                fail();
        }

    }

    @Test
    public void testSimpleAgent () throws Exception {
        int counter = Ticks;
        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.SIMPLE);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.SIMPLE);
        charlie = new GenteStrategyAgent (c, Color.RED, GenteStrategy.SIMPLE);
        denise = new GenteStrategyAgent (d, Color.GREEN, GenteStrategy.SIMPLE);

        game.registerAgent(alice);
        game.registerAgent(bob);
        game.registerAgent(charlie);
        game.registerAgent(denise);

        while (game.getState().equals(GameState.PLAY) && counter > 0) {
            boolean moved = false;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    GenteStrategyAgent agent = (GenteStrategyAgent) player;
                    Set<Move> moves = agent.determineMoves(game);
                    for (Move move : moves) {
                        game.move(move);
                        assertEquals (MoveStatus.EVALUATED, move.getStatus());
                        moved = true;
                        break;
                    }
                    counter--;
                 }
            }
            if (!moved)
                fail();
        }
    }

    public void testRandomAgent () throws Exception {
        int counter = Ticks;
        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.RANDOM);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.RANDOM);
        charlie = new GenteStrategyAgent (c, Color.RED, GenteStrategy.RANDOM);
        denise = new GenteStrategyAgent (d, Color.GREEN, GenteStrategy.RANDOM);

        game.registerAgent(alice);
        game.registerAgent(bob);
        game.registerAgent(charlie);
        game.registerAgent(denise);

        while (game.getState().equals(GameState.PLAY) && counter > 0) {
            boolean moved = false;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    GenteStrategyAgent agent = (GenteStrategyAgent) player;
                    Set<Move> moves = agent.determineMoves(game);
                    for (Move move : moves) {
                        game.move(move);
                        assertEquals (MoveStatus.EVALUATED, move.getStatus());
                        moved = true;
                        break;
                    }
                    counter--;
                 }
            }
            if (!moved)
                fail();
        }

    }
}
