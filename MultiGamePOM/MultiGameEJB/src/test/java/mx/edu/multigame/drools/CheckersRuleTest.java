/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

package mx.edu.multigame.drools;

import java.io.InputStreamReader;

import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;

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

public class CheckersRuleTest extends RulesTestBase {
	
	private RuleBase ruleset;

	private Game game;

	private StatefulSession session;

	private RulesTestEventListener rulesListener;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		PackageBuilder builder = new PackageBuilder();
		InputStreamReader reader = new InputStreamReader(this.getClass()
				.getResourceAsStream("/mx/ecosur/multigame/checkers.drl"));
		builder.addPackageFromDrl(reader);
		if (reader != null)
			reader.close();

		/*
		 * Create the StatefulSession, push the game into working memory, set
		 * the agenda group to "initialize" and start the game
		 */
		ruleset = RuleBaseFactory.newRuleBase();
		ruleset.addPackage(builder.getPackage());

		session = ruleset.newStatefulSession();
		rulesListener = new RulesTestEventListener();
		session.addEventListener(rulesListener);

		game = new Game();
		game.initialize(GameType.CHECKERS);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		session.dispose();
	}

	@Test
	public void testInitialize() {
		session.setFocus("initialize");
		game.setState(GameState.BEGIN);
		FactHandle gameHandle = session.insert(game);
		session.fireAllRules();

		Game modifiedGame = (Game) session.getObject(gameHandle);
		GameGrid grid = modifiedGame.getGrid();

		assertEquals(grid, this.rulesListener.localGrid);

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
