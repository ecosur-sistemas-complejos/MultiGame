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

import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.impl.CellComparator;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GameGrid;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.junit.Test;


public class GenteAgentLogicTest extends GenteAgentTestBase {
	
	private static boolean DEBUG = false;
	
	
	@Test
	/* Simple test to check the Available move logic in GenteStrategyAgent */
	public void testAvailableMoves () {
		TreeSet<GenteMove> unbound = alice.determineAvailableMoves();
		assertEquals (11, unbound.size());
	}
	
	@Test
	/* Simple test to check the Scoring move logic in GenteStrategyAgent */
	public void testScoringMoves () {
		/* Setup a hash of valid move destinations to compare against */
		TreeSet<GridCell> validDestinations = new TreeSet<GridCell> (
				new CellComparator());
		validDestinations.add(new GridCell (9,11, Color.YELLOW));
		validDestinations.add(new GridCell (13,7, Color.YELLOW));
		validDestinations.add(new GridCell (11,10,Color.RED));
		validDestinations.add(new GridCell (11,7, Color.RED));
		validDestinations.add(new GridCell (9,11, Color.BLUE));
		validDestinations.add(new GridCell (9,8, Color.BLUE));
		validDestinations.add(new GridCell (10,9, Color.BLUE));
		validDestinations.add(new GridCell (6,13, Color.GREEN));
		
		TreeSet<GenteMove> scoringMoves = alice.determineScoringMoves(Color.YELLOW);
		assertEquals (2, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestination();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		scoringMoves = alice.determineScoringMoves(Color.RED);
		assertEquals (2, scoringMoves.size());	
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestination();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}

		scoringMoves = alice.determineScoringMoves(Color.BLUE);
		assertEquals (3, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestination();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		
		scoringMoves = alice.determineScoringMoves(Color.GREEN);
		assertEquals (1, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestination();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		
		
		scoringMoves = alice.determineScoringMoves(alice.oppositionColors());
		assertEquals (4, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestination();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
	}
	
	@Test
	public void testRandomNextMove () throws InvalidMoveException {
		alice.setTurn(true);
		GenteMove next = fireRules (alice);
		assertNotNull (next);
		game.move(next);
		/* Validate that the move was made */
		assertEquals (next.getDestination(), game.getGrid().getLocation(
				(GridCell) next.getDestination()));
	}
	
	public void testRandomMoveOnEmptyBoard () throws InvalidMoveException {
		/* Reset the Grid */
		game.setGrid(new GameGrid());
		game.setState(GameState.PLAY);
		
		/* Run the same code in test GridMove but on a simpler board */
		testRandomNextMove ();
	}
	
	@Test
	public void testBlockerNextMove () throws InvalidMoveException {
		bob.setTurn(true);
		GenteMove next = fireRules (bob);
		assertNotNull (next);
		/* Ensure that the last move blocks a score by blue or green */
		GridCell destination = (GridCell) next.getDestination();
		if (destination.getColumn() == 11) {
			if (destination.getRow() != 10 && destination.getRow () != 7)
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
		game.move(next);
		assertEquals (next.getDestination(), game.getGrid().getLocation(
				(GridCell) next.getDestination()));
	}
	
	@Test
	public void testSimpleNextMove () throws InvalidMoveException {
		charlie.setTurn(true);
		GenteMove next = fireRules (charlie);
		assertNotNull (next);
		/* Validate that the next move is possible */
		game.move (next);
		assertTrue (next.getStatus() == MoveStatus.EVALUATED);		
	}
	
	public void testSimpleMoveOnStartingBoard () throws InvalidMoveException {
		/* Reset the Grid */
		GameGrid grid = game.getGrid();
		HashSet<GridCell> cells = new HashSet<GridCell> ();
		cells.add(new GridCell(10,10, Color.YELLOW));
		cells.add(new GridCell(9,9, Color.BLUE));
		grid.setCells(cells);
		game.setGrid(grid);
		game.setState(GameState.PLAY);
		
		charlie.setTurn(true);
		GenteMove next = fireRules (charlie);
		assertNotNull (next);
		/* Validate that the next move is possible */
		game.move (next);
		assertEquals (MoveStatus.EVALUATED, next.getStatus());		
	}
	
	
	private GenteMove fireRules(GenteStrategyAgent player) {
		RuleBase rules = player.getStrategy().getRuleBase();
		StatefulSession statefulSession = rules.newStatefulSession();
		if (DEBUG)
			statefulSession.addEventListener(new DebugEventListener());
		statefulSession.insert(player);
		statefulSession.insert(game);
		statefulSession.fireAllRules();
		return (GenteMove) player.nextMove();
	}
}
