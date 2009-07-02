package mx.edu.multigame.drools;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.model.implementation.GameImpl;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GentePlayer;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;
import mx.ecosur.multigame.impl.enums.gente.GenteStrategy;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.impl.model.GameGrid;

import org.junit.Before;

public abstract class GenteAgentTestBase extends RulesTestBase {
	
	protected GenteStrategyAgent alice, bob, charlie;
	
	protected GenteGame game;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		game = new GenteGame();
		
		GridRegistrant a = new GridRegistrant ("alice");
		GridRegistrant b = new GridRegistrant ("bob");
		GridRegistrant c = new GridRegistrant ("charlie");
		GridRegistrant d = new GridRegistrant ("denise");
		
		alice = new GenteStrategyAgent (game, a, Color.YELLOW, GenteStrategy.RANDOM);
		bob = new GenteStrategyAgent (game, b, Color.BLUE, GenteStrategy.BLOCKER);
		charlie = new GenteStrategyAgent (game, c, Color.RED, GenteStrategy.SIMPLE);
		
		game.updatePlayer (alice);
		game.updatePlayer(bob);
		game.updatePlayer(charlie);
		game.updatePlayer(new GentePlayer (game, d, Color.GREEN));
		game.initialize();
		
		setupBoard(game);
		game.initialize();
	}


	/*
	 * Sets the board up for testing. As these tests are meant to check
	 * the logic of the 
	 * 
	 */
	private void setupBoard(GenteGame game) {
		GameGrid grid = game.getGrid();
		
		GridCell yellow1 = new GridCell (10,10, Color.YELLOW);
		GridCell yellow2 = new GridCell (12,8, Color.YELLOW);
		grid.updateCell(yellow1);
		grid.updateCell(yellow2);
		
		GridCell blue1 = new GridCell (9,10, Color.BLUE);
		GridCell blue2 = new GridCell (9,9, Color.BLUE);
		grid.updateCell(blue1);
		grid.updateCell(blue2);
		
		GridCell red1 = new GridCell (11,9, Color.RED);
		GridCell red2 = new GridCell (11,8, Color.RED);
		grid.updateCell(red1);
		grid.updateCell(red2);
		
		GridCell green1 = new GridCell (8, 11, Color.GREEN);
		GridCell green2 = new GridCell (7,12, Color.GREEN);
		grid.updateCell(green1);
		grid.updateCell(green2);
		
		game.setGrid(grid);
		game.setState(GameState.BEGIN);
	}
}
