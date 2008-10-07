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

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;
import mx.ecosur.multigame.ejb.entity.pente.StrategyPlayer;
import mx.ecosur.multigame.ejb.jms.pente.StrategyPlayerListener;
import mx.ecosur.multigame.pente.BeadString;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.junit.Test;


public class AgentLogicTest extends AgentTestBase {
	
	private static boolean DEBUG = false;
	
	
	@Test
	/* Simple test to check the Available move logic in StrategyPlayer */
	public void testAvailableMoves () {
		TreeSet<PenteMove> unbound = alice.determineAvailableMoves();
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
		validDestinations.add(new Cell (9,11, Color.BLUE));
		validDestinations.add(new Cell (9,8, Color.BLUE));
		validDestinations.add(new Cell (10,9, Color.BLUE));
		validDestinations.add(new Cell (6,13, Color.GREEN));
		
		TreeSet<PenteMove> scoringMoves = alice.determineScoringMoves(Color.YELLOW);
		assertEquals (2, scoringMoves.size());
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		scoringMoves = alice.determineScoringMoves(Color.RED);
		assertEquals (2, scoringMoves.size());	
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}

		scoringMoves = alice.determineScoringMoves(Color.BLUE);
		assertEquals (3, scoringMoves.size());
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		
		scoringMoves = alice.determineScoringMoves(Color.GREEN);
		assertEquals (1, scoringMoves.size());
		for (PenteMove move : scoringMoves) {
			Cell destination = move.getDestination();
			SortedSet<Cell> tail = validDestinations.tailSet(destination);
			assertTrue (destination + " is not valid!", 
					tail.contains(destination));
		}
		
		
		scoringMoves = alice.determineScoringMoves(alice.oppositionColors());
		assertEquals (4, scoringMoves.size());
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
