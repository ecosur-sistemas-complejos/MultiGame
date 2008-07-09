package mx.edu.multigame.drools;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.event.ObjectInsertedEvent;
import org.drools.event.ObjectRetractedEvent;
import org.drools.event.ObjectUpdatedEvent;
import org.drools.event.WorkingMemoryEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PenteRulesTest {
	
	private static RuleBase Ruleset;
	
	private static boolean DEBUG = false;
	
	private Game game;
	
	private GamePlayer alice, bob, charlie, denise;

	private StatefulSession statefulSession;
	
	/** Static Initializer only loads rules once from the file system */
	static {
		PackageBuilder builder = new PackageBuilder();
		InputStreamReader reader = new InputStreamReader( 
				PenteRulesTest.class.getResourceAsStream(
					"/mx/ecosur/multigame/pente.drl"));
		try {
			builder.addPackageFromDrl(reader);
			Ruleset = RuleBaseFactory.newRuleBase();
			Ruleset.addPackage( builder.getPackage() );
		} catch (DroolsParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	
	@Before
	public void before () throws Exception {;
		game = new PenteGame ();
		game.initialize(GameType.PENTE);
		statefulSession = Ruleset.newStatefulSession();
		if (DEBUG)
			statefulSession.addEventListener(new DebugEventListener());
		Player a, b, c, d;
		a = new Player ("alice");
		b = new Player ("bob");
		c = new Player ("charlie");
		d = new Player ("denise");
		
		alice = new GamePlayer (game, a, Color.BLACK);
		bob = new GamePlayer (game, b, Color.BLUE);
		charlie = new GamePlayer (game, c, Color.GREEN);
		denise = new GamePlayer (game, d, Color.RED);
		
		game.addPlayer(alice);
		game.addPlayer(bob);
		game.addPlayer(charlie);
		game.addPlayer(denise);
	}
	
	@After
	public void after () {
		statefulSession.dispose();
	}
	
	@Test
	public void testInitialize () {
		game.setState(GameState.BEGIN);
		statefulSession.insert(game);
		statefulSession.setFocus("initialize");
		statefulSession.fireAllRules();
	
		assertTrue (game.getGrid().getCells().size() == 0);
		List<GamePlayer> players = game.getPlayers();
		
		GamePlayer p = players.get(players.indexOf(alice));
		assertNotNull (p);
		assertEquals ("alice", p.getPlayer().getName());
		assertEquals (true, p.isTurn());
	}
	
	@Test
	public void testFirstMoveValidate ()  {
		int row = game.getRows() / 2;
		int col = game.getColumns() / 2;
		Cell center = new Cell (row, col, alice.getColor());
		PenteMove move = new PenteMove (alice, center);
		
		game.setState(GameState.BEGIN);
		statefulSession.insert(game);
		statefulSession.setFocus("initialize");
		statefulSession.fireAllRules();
		statefulSession.clearAgenda();
		statefulSession.insert(move);
		statefulSession.setFocus ("verify");
		statefulSession.fireAllRules();
		
		assertEquals (Move.Status.VERIFIED, move.getStatus());
	}
	
	@Test 
	public void testExecuteFirstMove ()  {
		int row = game.getRows() / 2;
		int col = game.getColumns() / 2;
		Cell center = new Cell (row, col, alice.getColor());
		PenteMove move = new PenteMove (alice, center);
		
		game.setState(GameState.BEGIN);
		statefulSession.insert(game);
		statefulSession.setFocus("initialize");
		statefulSession.fireAllRules();
		statefulSession.insert(move);
		statefulSession.setFocus ("verify");
		statefulSession.fireAllRules();
		statefulSession.setFocus ("move");
		statefulSession.fireAllRules();
		
		assertEquals (Move.Status.MOVED, move.getStatus());
		assertEquals (center, game.getGrid().getLocation(center));
		
	}
	
	@Test
	public void testValidateSubsequentMove () {
		alice.setTurn (true);
		Cell center = new Cell (10, 10, alice.getColor());
		game.getGrid().updateCell(center);
		game.setState(GameState.PLAY);
		
		Cell next = new Cell (10,9, alice.getColor());
		PenteMove subsequent = new PenteMove (alice, next);
		
		statefulSession.insert(game);
		statefulSession.insert(subsequent);
		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();
		
		assertEquals (Move.Status.VERIFIED, subsequent.getStatus());
		
	}

	@Test
	public void testExecuteSubsequentMove () {
		alice.setTurn(true);
		Cell center = new Cell (10, 10, alice.getColor());
		game.getGrid().updateCell(center);
		game.setState(GameState.PLAY);
		
		Cell next = new Cell (10, 9, alice.getColor());
		PenteMove subsequent = new PenteMove (alice, next);
		
		statefulSession.insert(game);
		statefulSession.insert(subsequent);
		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();
		statefulSession.setFocus ("move");
		statefulSession.fireAllRules();
		
		assertEquals (Move.Status.MOVED, subsequent.getStatus());
		assertEquals (next, game.getGrid().getLocation(next));
	}
	
	private class DebugEventListener implements WorkingMemoryEventListener {	
		private Logger logger;
		
		public DebugEventListener () {
			logger = Logger.getLogger(PenteRulesTest.class.getCanonicalName());
		}

		public void objectInserted(ObjectInsertedEvent event) {
			//logger.info(event.getObject().toString());
		}

		public void objectRetracted(ObjectRetractedEvent event) {
			//logger.info(event.getOldObject().toString());
		}

		public void objectUpdated(ObjectUpdatedEvent event) {
			logger.info (event.getObject().toString());
		}
	}
}
