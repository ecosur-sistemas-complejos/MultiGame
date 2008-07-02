package mx.edu.multigame.drools;

import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;

import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Game;

import org.drools.FactHandle;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.PackageBuilder;
import org.junit.Before;
import org.junit.Test;

public class PenteRulesTest {
	
	private RuleBase ruleset;
	
	private Game game;
	
	@Before
	public void loadRules () throws Exception {
		PackageBuilder builder = new PackageBuilder();
		InputStreamReader reader = new InputStreamReader( 
				this.getClass().getResourceAsStream(
					"/mx/ecosur/multigame/pente.drl"));
		builder.addPackageFromDrl(reader);
		ruleset = RuleBaseFactory.newRuleBase();
		ruleset.addPackage( builder.getPackage() );
		
		game = new Game();
		game.initialize(GameType.PENTE);
		
	}
	
	@Test
	public void testInitialize () {
		
		StatefulSession session = ruleset.newStatefulSession(false);
		
		session.setFocus("initialize");
		game.setState(GameState.BEGIN);
		FactHandle gameHandle = session.insert(game);
		session.fireAllRules();
		
		Game modifiedGame = (Game) session.getObject(gameHandle);
		GameGrid grid = modifiedGame.getGrid();
		
		assertTrue (grid.getCells().size() == 0);
		
	}
}
