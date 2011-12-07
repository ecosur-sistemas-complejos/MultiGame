/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

import mx.ecosur.multigame.gente.entity.GenteGame;
import mx.ecosur.multigame.gente.entity.GentePlayer;
import mx.ecosur.multigame.gente.entity.GenteStrategyAgent;
import mx.ecosur.multigame.gente.enums.GenteStrategy;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.DummyMessageSender;
import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.grid.entity.GameGrid;

import org.junit.Before;

import java.util.TreeSet;

public abstract class GenteAgentTestBase extends GenteTestBase {
        
    protected GenteStrategyAgent alice, bob, charlie;
        
    protected GenteGame game;

    protected static GenteStrategy simple = GenteStrategy.SIMPLE, random = GenteStrategy.RANDOM;

    static {
        simple.getRuleBase();
        random.getRuleBase();
    }

    @Before
    public void setUp() throws Exception {
        game = new GenteGame();
        game.setMessageSender(new DummyMessageSender());

        GridRegistrant a = new GridRegistrant ("alice");
        GridRegistrant b = new GridRegistrant ("bob");
        GridRegistrant c = new GridRegistrant ("charlie");
        GridRegistrant d = new GridRegistrant ("denise");

        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.RANDOM);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.RANDOM);
        charlie = new GenteStrategyAgent(c, Color.RED, GenteStrategy.RANDOM);
        game.updatePlayer (alice);
        game.updatePlayer(bob);
        game.updatePlayer(charlie);
        game.updatePlayer(new GentePlayer(d, Color.GREEN));
        game.initialize();

        setupBoard(game);
    }


    /*
     * Sets the board up for testing. As these tests are meant to check
     * the logic of the
     *
     */
    private void setupBoard(GenteGame game) {
        GameGrid grid = game.getGrid();

        GridCell yellow1 = new GridCell (10,10, Color.YELLOW);
        GridCell yellow2 = new GridCell (10,11, Color.YELLOW);
        GridCell blue1 = new GridCell (9,10, Color.BLUE);
        GridCell blue2 = new GridCell (9,9, Color.BLUE);
        GridCell red1 = new GridCell (11,9, Color.RED);
        GridCell red2 = new GridCell(11,8, Color.RED);
        GridCell green1 = new GridCell (7, 11, Color.GREEN);
        GridCell green2 = new GridCell (7,12, Color.GREEN);

        setIds (yellow1, yellow2, blue1, blue2, red1, red2, green1, green2);

        if (grid.getCells() == null)
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));

        grid.getCells().add(yellow1);
        grid.getCells().add(yellow2);
        grid.getCells().add(blue1);
        grid.getCells().add(blue2);
        grid.getCells().add(red1);
        grid.getCells().add(red2);
        grid.getCells().add(green1);
        grid.getCells().add(green2);

        game.setGrid(grid);
    }
}
