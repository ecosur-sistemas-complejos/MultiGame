package mx.edu.multigame.drools;

import java.io.IOException;
import java.io.InputStreamReader;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.ejb.entity.pente.PentePlayer;
import mx.ecosur.multigame.ejb.entity.pente.StrategyPlayer;
import mx.ecosur.multigame.pente.PenteStrategy;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.junit.Before;

public abstract class AgentTestBase extends RulesTestBase {
	
	protected static RuleBase GameRuleset;

	protected Game game;
	
	protected StrategyPlayer alice, bob, charlie;
	
	static {
		PackageBuilder builder = new PackageBuilder();
		InputStreamReader reader = new InputStreamReader(PenteRulesTest.class
				.getResourceAsStream("/mx/ecosur/multigame/gente.drl"));
		try {
			builder.addPackageFromDrl(reader);
			GameRuleset = RuleBaseFactory.newRuleBase();
			GameRuleset.addPackage( builder.getPackage() );
		} catch (DroolsParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		game = new PenteGame();
		game.initialize(GameType.PENTE);
		
		Player a = new Player ("alice");
		Player b = new Player ("bob");
		Player c = new Player ("charlie");
		Player d = new Player ("denise");
		
		alice = new StrategyPlayer (game, a, Color.YELLOW, PenteStrategy.RANDOM);
		bob = new StrategyPlayer (game, b, Color.BLUE, PenteStrategy.BLOCKER);
		charlie = new StrategyPlayer (game, c, Color.RED, PenteStrategy.SIMPLE);
		
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
