package mx.edu.multigame.drools;

import static org.junit.Assert.*;

import java.awt.Dimension;
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
import org.drools.event.ObjectInsertedEvent;
import org.drools.event.ObjectRetractedEvent;
import org.drools.event.ObjectUpdatedEvent;
import org.drools.event.WorkingMemoryEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CheckersRuleTest {

	private RuleBase ruleset;
	
	private Game game;

	private StatefulSession session;

	private RulesTestEventListener rulesListener;
	
	@Before
	public void fixtures () throws Exception {
		PackageBuilder builder = new PackageBuilder();
		InputStreamReader reader = new InputStreamReader( 
					this.getClass().getResourceAsStream(
						"/mx/ecosur/multigame/checkers.drl"));
		builder.addPackageFromDrl(reader);
		if (reader != null)
			reader.close();
		
			/* Create the StatefulSession, push the game into working memory,
			 * set the agenda group to "initialize" and start the game 
			 */
		 ruleset = RuleBaseFactory.newRuleBase();
		 ruleset.addPackage( builder.getPackage() );
		 
		 session = ruleset.newStatefulSession(false);
		 rulesListener = new RulesTestEventListener();
		 session.addEventListener(rulesListener);
		 
		 game = new Game ();
		 game.initialize(GameType.CHECKERS);
	}
	
	@After
	public void cleanUp () {
		session.dispose();
	}
	
	@Test
	public void testInitialize () {
		session.setFocus("initialize");
		game.setState(GameState.BEGIN);
		FactHandle gameHandle = session.insert(game);
		session.fireAllRules();
		
		Game modifiedGame = (Game) session.getObject(gameHandle);
		GameGrid grid = modifiedGame.getGrid();
		
		assertEquals (grid, this.rulesListener.localGrid);
		
	}
	
	class RulesTestEventListener implements WorkingMemoryEventListener {

		private GameGrid localGrid;

		public void objectInserted(ObjectInsertedEvent event) {
			if (event.getObject() instanceof Game)
				this.localGrid = ((Game) event.getObject()).getGrid();
		}

		public void objectRetracted(ObjectRetractedEvent event) {
			if (event.getOldObject() instanceof Game)
				this.localGrid = null;
			
		}

		public void objectUpdated(ObjectUpdatedEvent event) {
			if (event.getObject() instanceof Game)
				this.localGrid = ((Game) event.getObject()).getGrid();
		}
		
	}

}
