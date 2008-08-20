package mx.edu.multigame.drools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
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
import mx.ecosur.multigame.ejb.entity.pente.PentePlayer;
import mx.ecosur.multigame.pente.BeadString;

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

public class PenteRulesTest extends RulesTestBase {
	
	private static RuleBase Ruleset;
	
	private static boolean DEBUG = false;
	
	private Game game;
	
	private PentePlayer alice, bob, charlie, denise;

	private StatefulSession statefulSession;
	
	/** Static Initializer only loads rules once from the file system */
	static {
		PackageBuilder builder = new PackageBuilder();
		InputStreamReader reader = new InputStreamReader(PenteRulesTest.class
				.getResourceAsStream("/mx/ecosur/multigame/gente.drl"));
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
	public void setUp() throws Exception {

		super.setUp();
		
		game = new PenteGame();
		game.initialize(GameType.PENTE);
		statefulSession = Ruleset.newStatefulSession();
		if (DEBUG)
			statefulSession.addEventListener(new DebugEventListener());
		Player a, b, c, d;
		a = new Player ("alice");
		b = new Player ("bob");
		c = new Player ("charlie");
		d = new Player ("denise");
		
		alice = new PentePlayer (game, a, Color.YELLOW);
		bob = new PentePlayer (game, b, Color.BLUE);
		charlie = new PentePlayer (game, c, Color.GREEN);
		denise = new PentePlayer (game, d, Color.RED);
		
		game.addPlayer(alice);
		game.addPlayer(bob);
		game.addPlayer(charlie);
		game.addPlayer(denise);
	}
	
	@After
	public void tearDown() throws Exception {
		super.tearDown();
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
		game.setState(GameState.BEGIN);
		alice.setGame(game);
		PenteMove move = new PenteMove (alice, center);
		
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
		
		game.setState(GameState.BEGIN);
		PenteMove move = new PenteMove (alice, center);
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
		
		fireRules (subsequent);
		
		assertEquals (Move.Status.MOVED, subsequent.getStatus());
		assertEquals (next, game.getGrid().getLocation(next));
	}
	
	@Test
	public void testFindTheTrias () {
		alice.setTurn(true);
		Cell start = new Cell (4,4,alice.getColor());
		Cell second = new Cell (4,3,alice.getColor());
		
		game.getGrid().updateCell(start);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		Cell tessera = new Cell (4,5, alice.getColor());
		PenteMove move = new PenteMove (alice, tessera);
		
		fireRules (move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		HashSet<BeadString> set = move.getTrias();
		assertEquals (1, set.size());
	}
	
	@Test
	public void testFindTheTesseras () {
		alice.setTurn(true);
		Cell start = new Cell (4,4,alice.getColor());
		Cell second = new Cell (4,3,alice.getColor());
		Cell third = new Cell (4,2, alice.getColor().getCompliment());
		
		game.getGrid().updateCell(start);
		game.getGrid().updateCell(second);
		game.getGrid().updateCell(third);
		
		game.setState(GameState.PLAY);
		
		Cell tessera = new Cell (4,5, alice.getColor());
		PenteMove move = new PenteMove (alice, tessera);
		
		fireRules (move);
		
		assertEquals (Move.Status.MOVED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		HashSet<BeadString> set = move.getTesseras();
		assertEquals (1, set.size());		
		
	}
	
	@Test
	public void testSelfishScoring () {
		alice.setTurn(true);
		Cell first = new Cell (4,3,alice.getColor());
		Cell second = new Cell (4,4,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		Cell third = new Cell (3,6, alice.getColor());
		Cell fourth = new Cell (2,7, alice.getColor());
		
		game.getGrid ().updateCell(third);
		game.getGrid ().updateCell(fourth);
		
		game.setState(GameState.PLAY);
		
		Cell tria = new Cell (4,5, alice.getColor());
		PenteMove move = new PenteMove (alice, tria);
		
		fireRules (move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tria, game.getGrid().getLocation(tria));
		
		HashSet<BeadString> set = move.getTrias();
		/* There should be 2 sets of three */
		assertEquals (2, set.size());
		
		/* 2 sets of three equals 10 points */
		assertEquals (10, alice.getPoints());
		assertEquals (2, alice.getTrias().size());
	}
	
	@Test
	public void testCooperativeScoring () {
		alice.setTurn(true);
		
		/* Setup the first tessera */
		Cell start = new Cell (5,4,alice.getColor());
		Cell second = new Cell (5,5,alice.getColor().getCompliment());
		Cell third = new Cell (5,6, alice.getColor().getCompliment());
		
		game.getGrid().updateCell(start);
		game.getGrid().updateCell(second);
		game.getGrid().updateCell(third);
		
		/* Setup the second tessera */
		Cell fourth =  new Cell (2,7,alice.getColor().getCompliment());
		Cell fifth =  new Cell (3,7,alice.getColor().getCompliment());
		Cell sixth =  new Cell (4,7,alice.getColor().getCompliment());
		
		game.getGrid().updateCell(fourth);
		game.getGrid().updateCell(fifth);
		game.getGrid().updateCell(sixth);
		
		/* Setup the third tessera */
		Cell seventh =  new Cell (6,6,alice.getColor().getCompliment());
		Cell eighth =  new Cell (7,5,alice.getColor().getCompliment());
		Cell ninth =  new Cell (8,4,alice.getColor().getCompliment());
		
		game.getGrid().updateCell(seventh);
		game.getGrid().updateCell(eighth);
		game.getGrid().updateCell(ninth);
		
		game.setState(GameState.PLAY);
		
		Cell tessera = new Cell (5,7, alice.getColor());
		PenteMove move = new PenteMove (alice, tessera);
		
		fireRules (move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		HashSet<BeadString> set = move.getTesseras();
		assertEquals (3, set.size());		
		
		assertEquals (5, alice.getPoints());
		assertEquals (3, alice.getTesseras().size ());
		
		/* Check Alice's partner */
		PentePlayer partner = alice.getPartner();
		assertEquals (5, partner.getPoints());
		assertEquals  (3, partner.getTesseras().size());
	}
	
	@Test
	public void testDiagnolTessera () {
		alice.setTurn(true);
		Cell invalid = new Cell (8,8, alice.getColor());
		Cell first = new Cell (9,9,alice.getColor());
		Cell second = new Cell (11,11,alice.getColor());
		
		game.getGrid().updateCell(invalid);
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		Cell tessera = new Cell (10,10, alice.getColor());
		PenteMove move = new PenteMove (alice, tessera);
		
		fireRules (move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		HashSet<BeadString> trias = move.getTrias();
		HashSet<BeadString> tesseras = move.getTesseras();
		assertEquals (1, tesseras.size());
		assertEquals (0, trias.size());
	}

	@Test
	public void testDiagnolTria () {
		alice.setTurn(true);

		Cell first = new Cell (9,9,alice.getColor());
		Cell second = new Cell (11,11,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		Cell tessera = new Cell (10,10, alice.getColor());
		PenteMove move = new PenteMove (alice, tessera);
		
		fireRules (move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		HashSet<BeadString> trias = move.getTrias();
		assertEquals (1, trias.size());
	}
	
	@Test
	public void testInvalidDiagnolTria () {
		alice.setTurn(true);

		Cell first = new Cell (9,9,alice.getColor());
		Cell second = new Cell (10,10,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		Cell tessera = new Cell (12,12, alice.getColor());
		PenteMove move = new PenteMove (alice, tessera);
		
		fireRules (move);
		
		assertEquals (Move.Status.MOVED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		HashSet<BeadString> trias = move.getTrias();
		assertEquals (0, trias.size());		
	}
	
	@Test
	public void testMixedTessera () {
		alice.setTurn(true);
		Cell invalid = new Cell (8,8, alice.getColor());
		Cell first = new Cell (8,9,alice.getColor().getCompliment());
		Cell second = new Cell (8,11,alice.getColor());
		
		game.getGrid().updateCell(invalid);
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		Cell tessera = new Cell (8,10, alice.getColor());
		PenteMove move = new PenteMove (alice, tessera);
		
		fireRules (move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		HashSet<BeadString> trias = move.getTrias();
		HashSet<BeadString> tesseras = move.getTesseras();
		assertEquals (1, tesseras.size());
		assertEquals (0, trias.size());		
	}
	
	@Test
	public void testJoinedTrias () {
		alice.setTurn(true);

		Cell first = new Cell (8,10,alice.getColor());
		Cell second = new Cell (9,10,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		Cell third = new Cell (10,8,alice.getColor());
		Cell fourth = new Cell (10,9,alice.getColor());
		
		game.getGrid().updateCell(third);
		game.getGrid().updateCell(fourth);
		
		game.setState(GameState.PLAY);
		
		Cell tria = new Cell (10,10, alice.getColor());
		PenteMove move = new PenteMove (alice, tria);
		
		fireRules (move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tria, game.getGrid().getLocation(tria));
		
		HashSet<BeadString> trias = move.getTrias();
		assertEquals (2, trias.size());
	}
	
	@Test
	public void testMixedTriaTessera () {
		alice.setTurn(true);
		Cell first = new Cell (10,8,alice.getColor().getCompliment());
		Cell second = new Cell (10,9,alice.getColor().getCompliment());
		Cell third = new Cell (10,11, alice.getColor().getCompliment());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		game.getGrid().updateCell(third);
		
		game.setState(GameState.PLAY);
		alice.setGame(game);
		
		Cell tessera = new Cell (10,10, alice.getColor());
		PenteMove move = new PenteMove (alice, tessera);
		
		fireRules(move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		HashSet<BeadString> trias = move.getTrias();
		assertEquals (0, trias.size());
		
		HashSet<BeadString> tesseras = move.getTesseras();
		assertEquals (1, tesseras.size());
		
	}
	
	public void testTwoUnrelatedTrias () {
		alice.setTurn(true);

		Cell first = new Cell (8,10,alice.getColor());
		Cell second = new Cell (9,10,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		game.setState(GameState.PLAY);
		alice.setGame(game);
		
		Cell tria = new Cell (10,10, alice.getColor());
		PenteMove move = new PenteMove (alice, tria);
		
		fireRules (move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tria, game.getGrid().getLocation(tria));
		
		HashSet<BeadString> trias = move.getTrias();
		assertEquals (1, trias.size());
		assertEquals (1, alice.getTrias().size());

		first = new Cell (8,12,alice.getColor());
		second = new Cell (9,12,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		alice.setGame(game);
		
		tria = new Cell (10,12, alice.getColor());
		move = new PenteMove (alice, tria);
		
		fireRules(move);
		
		assertEquals (Move.Status.EVALUATED, move.getStatus());
		assertEquals (tria, game.getGrid().getLocation(tria));
		
		trias = move.getTrias();
		assertEquals (1, trias.size());
		assertEquals (2, alice.getTrias().size());
		
	}

	private void fireRules(PenteMove move) {
		statefulSession.insert(game);
		statefulSession.insert(move);
		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();
		statefulSession.setFocus ("move");
		statefulSession.fireAllRules();
		statefulSession.setFocus ("evaluate");
		statefulSession.fireAllRules();
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
