/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.edu.multigame.drools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;
import mx.ecosur.multigame.ejb.entity.pente.PentePlayer;
import mx.ecosur.multigame.ejb.entity.pente.StrategyPlayer;
import mx.ecosur.multigame.pente.BeadString;
import mx.ecosur.multigame.pente.PenteStrategy;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.junit.Before;
import org.junit.Test;


public class AgentLogicTest extends RulesTestBase {
	
	private static boolean DEBUG = false;
	
	private static RuleBase GameRuleset;

	private Game game;
	
	private StrategyPlayer alice, bob, charlie;
	
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
	}
	
	@Test
	/* Simple test to check the Available move logic in StrategyPlayer */
	public void testAvailableMoves () {
		HashSet<PenteMove> unbound = alice.getAvailableMoves();
		assertEquals (11, unbound.size());
	}
	
	@Test
	/* Simple test to check the Scoring move logic in StrategyPlayer */
	public void testScoringMoves () {
		/* Setup a hash of valid move destinations to compare against */
		TreeSet<Cell> validDestinations = new TreeSet<Cell> (
				new CellComparator());
		validDestinations.add(new Cell (9,11, Color.YELLOW));
		validDestinations.add(new Cell (13,7, Color.YELLOW));
		validDestinations.add(new Cell (11,10,Color.RED));
		validDestinations.add(new Cell (11,7, Color.RED));
		validDestinations.add(new Cell (13,7, Color.RED));
		validDestinations.add(new Cell (6,13, Color.BLUE));
		validDestinations.add(new Cell (10,9, Color.BLUE));
		validDestinations.add(new Cell (9,11, Color.BLUE));
		validDestinations.add(new Cell (10,9, Color.GREEN));
		validDestinations.add(new Cell (6,13, Color.GREEN));
		
		HashSet<PenteMove> scoringMoves = alice.getScoringMoves(Color.YELLOW);
		assertEquals (2, scoringMoves.size());
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		scoringMoves = alice.getScoringMoves(Color.RED);
		assertEquals (2, scoringMoves.size());	
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}

		scoringMoves = alice.getScoringMoves(Color.BLUE);
		assertEquals (3, scoringMoves.size());
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		
		scoringMoves = alice.getScoringMoves(Color.GREEN);
		assertEquals (2, scoringMoves.size());
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		
		
		scoringMoves = alice.getScoringMoves(alice.getOppositionColors());
		assertEquals (5, scoringMoves.size());
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
	}
	
	@Test
	public void testRandomNextMove () {
		alice.setTurn(true);
		PenteMove next = fireRules (alice);
		assertNotNull (next);
		fireRules (next);
		/* Validate that the move was made */
		assertEquals (next.getDestination(), game.getGrid().getLocation(
				next.getDestination()));
	}
	
	@Test
	public void testBlockerNextMove () {
		bob.setTurn(true);
		PenteMove next = fireRules (bob);
		assertNotNull (next);
		/* Ensure that the last move blocks a score by blue or green */
		Cell destination = next.getDestination();
		if (destination.getColumn() == 11) {
			if (destination.getRow() != 10 || destination.getRow () != 7)
				fail("Destination [" + destination + "] does not block any available " +
				"enemy moves!");
		} else if (destination.getColumn() == 9) {
			assertEquals (11, destination.getRow());
		} else if (destination.getColumn() == 13) {
			assertEquals (7, destination.getRow());
		} else 
			fail("Destination [" + destination + "] does not block any available " +
					"enemy moves!");
		/* Validate that the next move was made, and is on the board */
		fireRules (next);
		assertEquals (next.getDestination(), game.getGrid().getLocation(
				next.getDestination()));
	}
	
	@Test
	public void testSimpleNextMove () {
		charlie.setTurn(true);
		PenteMove next = fireRules (charlie);
		assertNotNull (next);
		/* Validate that the next move is possible */
		fireRules (next);
		assertEquals (Move.Status.EVALUATED, next.getStatus());		
		/* The next move should have scored with a tessera, ensure that is 
		 * the case. */
		HashSet<BeadString> scoring = new HashSet<BeadString>();
		scoring.addAll(next.getTesseras());
		scoring.addAll(next.getTrias());
		assertEquals (1, scoring.size());
	}
	
	
	private PenteMove fireRules(StrategyPlayer player) {
		RuleBase rules = player.getStrategy().getRuleBase();
		StatefulSession statefulSession = rules.newStatefulSession();
		if (DEBUG)
			statefulSession.addEventListener(new DebugEventListener());
		statefulSession.insert(player);
		statefulSession.insert(game);
		statefulSession.fireAllRules();
		return player.getNextMove();
	}
	
	private void fireRules(PenteMove move) {
		StatefulSession penteSession = GameRuleset.newStatefulSession();
		if (DEBUG)
			penteSession.addEventListener(new DebugEventListener());
		penteSession.insert(game);
		penteSession.insert(move);
		penteSession.setFocus("verify");
		penteSession.fireAllRules();
		penteSession.setFocus ("move");
		penteSession.fireAllRules();
		penteSession.setFocus ("evaluate");
		penteSession.fireAllRules();
	}
}
