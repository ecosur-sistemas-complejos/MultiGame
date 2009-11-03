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
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GameGrid;

import org.junit.Test;


public class GenteAgentLogicTest extends GenteAgentTestBase {
	
	@Test
	/* Simple test to check the Available move logic in GenteStrategyAgent */
	public void testAvailableMoves () {
		TreeSet<GenteMove> unbound = alice.determineAvailableMoves(game);
		assertEquals (7, unbound.size());
	}


/*	public void testScoringMoves () throws InvalidMoveException {
		*//* Setup a hash of valid move destinations to compare against *//*
		TreeSet<GridCell> validDestinations = new TreeSet<GridCell> (
				new CellComparator());
		validDestinations.add(new GridCell (10,9, Color.YELLOW));
		validDestinations.add(new GridCell (10,12, Color.YELLOW));
		validDestinations.add(new GridCell (11,10,Color.RED));
		validDestinations.add(new GridCell (11,7, Color.RED));
		validDestinations.add(new GridCell (9,11, Color.BLUE));
		validDestinations.add(new GridCell (9,8, Color.BLUE));
		validDestinations.add(new GridCell (10,9, Color.BLUE));
		validDestinations.add(new GridCell (6,13, Color.GREEN));

		TreeSet<GenteMove> scoringMoves = alice.determineScoringMoves(game, Color.YELLOW);
		assertEquals (2, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestinationCell();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!",
					tail.contains(destination));
		}
		scoringMoves = alice.determineScoringMoves(game, Color.RED);
		assertEquals (2, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestinationCell();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!",
					tail.contains(destination));
		}

		scoringMoves = alice.determineScoringMoves(game, Color.BLUE);
		assertEquals (3, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestinationCell();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!",
					tail.contains(destination));
		}

		scoringMoves = alice.determineScoringMoves(game, Color.GREEN);
		assertEquals (1, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestinationCell();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!",
					tail.contains(destination));
		}


		scoringMoves = alice.determineScoringMoves(game, alice.oppositionColors());
		assertEquals (4, scoringMoves.size());
		for (GenteMove move : scoringMoves) {
			GridCell destination = (GridCell) move.getDestinationCell();
			SortedSet<GridCell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!",
					tail.contains(destination));
		}
	}*/
	
	@Test
	public void testRandomNextMove () throws InvalidMoveException {
		alice.determineNextMove(game);
		GenteMove next = alice.getNextMove();
		assertNotNull (next);
		game.move(next);
		/* Validate that the move was made */
		assertEquals (next.getDestinationCell(), game.getGrid().getLocation(
				(GridCell) next.getDestinationCell()));
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
		alice.setTurn(false);
		bob.setTurn(true);
		bob.determineNextMove(game);
		GenteMove next = (GenteMove) bob.getNextMove();
		assertNotNull (next);
		/* Ensure that the last move blocks a score by blue or green */
		GridCell destination = (GridCell) next.getDestinationCell();
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
		assertEquals (next.getDestinationCell(), game.getGrid().getLocation(
				(GridCell) next.getDestinationCell()));
	}
	
	@Test
	public void testSimpleNextMove () throws InvalidMoveException {
		charlie.setTurn(true);
		charlie.determineNextMove(game);
		GenteMove next = (GenteMove) charlie.getNextMove();
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
		charlie.determineNextMove(game);
		GenteMove next = (GenteMove) charlie.getNextMove();
		assertNotNull (next);
		/* Validate that the next move is possible */
		game.move (next);
		assertEquals (MoveStatus.EVALUATED, next.getStatus());		
	}
}
