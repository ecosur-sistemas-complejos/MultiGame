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
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.DummyMessageSender;
import mx.ecosur.multigame.grid.model.GridPlayer;
import mx.ecosur.multigame.grid.model.GridRegistrant;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;
import mx.ecosur.multigame.impl.enums.gente.GenteStrategy;
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

    private static final int Ticks = 50;

    @Before
    public void setUp() throws Exception {
        game = new GenteGame();
        game.setMessageSender(new DummyMessageSender());
        a = new GridRegistrant ("alice");
        b = new GridRegistrant ("bob");
        c = new GridRegistrant ("charlie");
        d = new GridRegistrant("denise");
    }

    @Test
    public void testSimpleAgent () throws Exception {
        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.SIMPLE);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.SIMPLE);
        charlie = new GenteStrategyAgent (c, Color.RED, GenteStrategy.SIMPLE);
        denise = new GenteStrategyAgent (d, Color.GREEN, GenteStrategy.SIMPLE);

        game.registerAgent(alice);
        game.registerAgent(bob);
        game.registerAgent(charlie);
        game.registerAgent(denise);
        int counter = 0;

        while (game.getState().equals(GameState.PLAY) && counter < Ticks) {
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    GenteStrategyAgent agent = (GenteStrategyAgent) player;
                    Set<Move> moves = agent.determineMoves(game);
                    Move move = moves.iterator().next();
                    game.move(move);
                    assertEquals (MoveStatus.EVALUATED, move.getStatus());
                 }
            }
            counter++;
        }

        assertEquals ("Expected " + counter + " moves but only " + game.getMoves().size() + " move(s) found!",
                Ticks * 4, game.getMoves().size());
    }

    @Test
    public void testRandomAgent () throws Exception {
        int counter = 0;
        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.RANDOM);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.RANDOM);
        charlie = new GenteStrategyAgent (c, Color.RED, GenteStrategy.RANDOM);
        denise = new GenteStrategyAgent (d, Color.GREEN, GenteStrategy.RANDOM);

        game.registerAgent(alice);
        game.registerAgent(bob);
        game.registerAgent(charlie);
        game.registerAgent(denise);

        while (game.getState().equals(GameState.PLAY) && counter < Ticks) {
            boolean moved = false;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    GenteStrategyAgent agent = (GenteStrategyAgent) player;
                    Set<Move> moves = agent.determineMoves(game);
                    Move move = moves.iterator().next();
                    game.move(move);
                    assertEquals (MoveStatus.EVALUATED, move.getStatus());
                 }
            }
            counter++;
        }

        assertEquals ("Expected " + counter + " moves but only " + game.getMoves().size() + " move(s) found!",
                Ticks * 4, game.getMoves().size());
    }
}
