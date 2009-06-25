package mx.edu.multigame.drools;

import java.io.IOException;
import java.io.InputStreamReader;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.impl.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.impl.ejb.entity.pente.PentePlayer;
import mx.ecosur.multigame.impl.ejb.entity.pente.PenteStrategyPlayer;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.impl.pente.PenteStrategy;
import mx.ecosur.multigame.model.Cell;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GameGrid;
import mx.ecosur.multigame.model.Player;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.junit.Before;

public abstract class AgentTestBase extends RulesTestBase {
	
	protected static RuleBase GameRuleset = GameType.PENTE.getRuleBase();

	protected Game game;
	
	protected PenteStrategyPlayer alice, bob, charlie;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		game = new PenteGame();
		game.initialize(GameType.PENTE);
		
		Player a = new Player ("alice");
		Player b = new Player ("bob");
		Player c = new Player ("charlie");
		Player d = new Player ("denise");
		
		alice = new PenteStrategyPlayer (game, a, Color.YELLOW, PenteStrategy.RANDOM);
		bob = new PenteStrategyPlayer (game, b, Color.BLUE, PenteStrategy.BLOCKER);
		charlie = new PenteStrategyPlayer (game, c, Color.RED, PenteStrategy.SIMPLE);
		
		game.addPlayer(alice);
		game.addPlayer(bob);
		game.addPlayer(charlie);
		game.addPlayer(new PentePlayer (game, d, Color.GREEN));
		
		setupBoard(game);

	}


	/*
	 * Sets the board up for testing. As these tests are meant to check
	 * the logic of the 
	 * 
	 */
	private void setupBoard(Game game) {
		GameGrid grid = game.getGrid();
		
		Cell yellow1 = new Cell (10,10, Color.YELLOW);
		Cell yellow2 = new Cell (12,8, Color.YELLOW);
		grid.updateCell(yellow1);
		grid.updateCell(yellow2);
		
		Cell blue1 = new Cell (9,10, Color.BLUE);
		Cell blue2 = new Cell (9,9, Color.BLUE);
		grid.updateCell(blue1);
		grid.updateCell(blue2);
		
		Cell red1 = new Cell (11,9, Color.RED);
		Cell red2 = new Cell (11,8, Color.RED);
		grid.updateCell(red1);
		grid.updateCell(red2);
		
		Cell green1 = new Cell (8, 11, Color.GREEN);
		Cell green2 = new Cell (7,12, Color.GREEN);
		grid.updateCell(green1);
		grid.updateCell(green2);
		
		game.setGrid(grid);
		game.setState(GameState.PLAY);
		
		alice.setTurn(true);
		game.updatePlayer(alice);
	}
}
