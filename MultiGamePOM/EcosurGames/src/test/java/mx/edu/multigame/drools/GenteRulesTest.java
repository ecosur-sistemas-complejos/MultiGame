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

import java.util.Collection;
import java.util.Set;


import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;
import mx.ecosur.multigame.impl.entity.gente.GentePlayer;

import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.impl.model.gente.BeadString;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GenteRulesTest extends RulesTestBase {
	
	private GenteGame game;
	
	private GentePlayer alice, bob, charlie, denise;
	
	
	@Before
	public void setUp() throws Exception {

		super.setUp();
		
		game = new GenteGame();
		GridRegistrant a, b, c, d;
		a = new GridRegistrant ("alice");
		b = new GridRegistrant ("bob");
		c = new GridRegistrant ("charlie");
		d = new GridRegistrant ("denise");
		
		alice = (GentePlayer) game.registerPlayer(a);
		bob = (GentePlayer) game.registerPlayer(b);
		charlie = (GentePlayer) game.registerPlayer(c);
		denise = (GentePlayer) game.registerPlayer(d);
	}
	
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	@Test
	public void testInitialize () {
		assertTrue (game.getGrid().getCells().size() == 0);
		Collection<GridPlayer> players = game.getPlayers();
		GentePlayer p = null;
		for (GridPlayer player : players) {
			if (player.getRegistrant().getName().equals("alice")) {
				p = (GentePlayer) player;
				break;
			}
		}
		
		assertNotNull (p);
		assertEquals ("alice", p.getRegistrant().getName());
		assertEquals (true, p.isTurn());
	}
	
	@Test 
	public void testExecuteFirstMove () throws InvalidMoveException  {		
		int row = game.getRows() / 2;
		int col = game.getColumns() / 2;
		GridCell center = new GridCell (row, col, alice.getColor());		
		GenteMove move = new GenteMove (alice, center);
		game.move (move);
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (center, game.getGrid().getLocation(center));
	}
	
	@Test
	public void testValidateSubsequentMove () throws InvalidMoveException {
		GridCell center = new GridCell (10, 10, alice.getColor());
		game.getGrid().updateCell(center);
		
		GridCell next = new GridCell (10,9, alice.getColor());
		GenteMove subsequent = new GenteMove (alice, next);
		game.move (subsequent);
		assertEquals (MoveStatus.EVALUATED, subsequent.getStatus());
	}

	@Test
	public void testExecuteSubsequentMove () throws InvalidMoveException {
		alice.setTurn(true);
		GridCell center = new GridCell (10, 10, alice.getColor());
		game.getGrid().updateCell(center);
		game.setState(GameState.PLAY);
		
		GridCell next = new GridCell (10, 9, alice.getColor());
		GenteMove subsequent = new GenteMove (alice, next);
		game.move(subsequent);
		assertEquals (MoveStatus.EVALUATED, subsequent.getStatus());
		assertEquals (next, game.getGrid().getLocation(next));
	}
	
	@Test
	public void testFindTheTrias () throws InvalidMoveException {
		GridCell start = new GridCell (4,4,alice.getColor());
		GridCell second = new GridCell (4,3,alice.getColor());
		
		game.getGrid().updateCell(start);
		game.getGrid().updateCell(second);
		
		GridCell tessera = new GridCell (4,5, alice.getColor());
		GenteMove move = new GenteMove (alice, tessera);
		
		game.move(move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		Set<BeadString> set = move.getTrias();
		assertEquals (1, set.size());
	}
	
	@Test
	public void testFindTheTesseras () throws InvalidMoveException {
		alice.setTurn(true);
		GridCell start = new GridCell (4,4,alice.getColor());
		GridCell second = new GridCell (4,3,alice.getColor());
		GridCell third = new GridCell (4,2, alice.getColor().getCompliment());
		
		game.getGrid().updateCell(start);
		game.getGrid().updateCell(second);
		game.getGrid().updateCell(third);
		
		GridCell tessera = new GridCell (4,5, alice.getColor());
		GenteMove move = new GenteMove (alice, tessera);
		
		game.move(move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		Set<BeadString> set = move.getTesseras();
		assertEquals (1, set.size());		
		
	}
	
	@Test
	public void testSelfishScoring () throws InvalidMoveException {
		alice.setTurn(true);
		GridCell first = new GridCell (4,3,alice.getColor());
		GridCell second = new GridCell (4,4,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		GridCell third = new GridCell (3,6, alice.getColor());
		GridCell fourth = new GridCell (2,7, alice.getColor());
		
		game.getGrid ().updateCell(third);
		game.getGrid ().updateCell(fourth);
		
		game.setState(GameState.PLAY);
		
		GridCell tria = new GridCell (4,5, alice.getColor());
		GenteMove move = new GenteMove (alice, tria);
		
		game.move(move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tria, game.getGrid().getLocation(tria));
		
		Set<BeadString> set = move.getTrias();
		/* There should be 2 sets of three */
		assertEquals (2, set.size());
		
		/* 2 sets of three equals 10 points */
		assertEquals (10, alice.getPoints());
		assertEquals (2, alice.getTrias().size());
	}
	
	@Test
	public void testCooperativeScoring () throws InvalidMoveException {
		alice.setTurn(true);
		
		/* Setup the first tessera */
		GridCell start = new GridCell (5,4,alice.getColor());
		GridCell second = new GridCell (5,5,alice.getColor().getCompliment());
		GridCell third = new GridCell (5,6, alice.getColor().getCompliment());
		
		game.getGrid().updateCell(start);
		game.getGrid().updateCell(second);
		game.getGrid().updateCell(third);
		
		/* Setup the second tessera */
		GridCell fourth =  new GridCell (2,7,alice.getColor().getCompliment());
		GridCell fifth =  new GridCell (3,7,alice.getColor().getCompliment());
		GridCell sixth =  new GridCell (4,7,alice.getColor().getCompliment());
		
		game.getGrid().updateCell(fourth);
		game.getGrid().updateCell(fifth);
		game.getGrid().updateCell(sixth);
		
		/* Setup the third tessera */
		GridCell seventh =  new GridCell (6,6,alice.getColor().getCompliment());
		GridCell eighth =  new GridCell (7,5,alice.getColor().getCompliment());
		GridCell ninth =  new GridCell (8,4,alice.getColor().getCompliment());
		
		game.getGrid().updateCell(seventh);
		game.getGrid().updateCell(eighth);
		game.getGrid().updateCell(ninth);
		
		game.setState(GameState.PLAY);
		
		GridCell tessera = new GridCell (5,7, alice.getColor());
		GenteMove move = new GenteMove (alice, tessera);
		
		game.move(move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		Set<BeadString> set = move.getTesseras();
		assertEquals (3, set.size());		
		
		assertEquals (5, alice.getPoints());
		assertEquals (3, alice.getTesseras().size ());
		
		/* Check Alice's partner */
		GentePlayer partner = alice.getPartner();
		assertEquals (5, partner.getPoints());
		assertEquals  (3, partner.getTesseras().size());
	}
	
	@Test
	public void testDiagnolTessera () throws InvalidMoveException {
		alice.setTurn(true);
		GridCell invalid = new GridCell (8,8, alice.getColor());
		GridCell first = new GridCell (9,9,alice.getColor());
		GridCell second = new GridCell (11,11,alice.getColor());
		
		game.getGrid().updateCell(invalid);
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		GridCell tessera = new GridCell (10,10, alice.getColor());
		GenteMove move = new GenteMove (alice, tessera);
		
		game.move(move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		Set<BeadString> trias = move.getTrias();
		Set<BeadString> tesseras = move.getTesseras();
		
		/* Tests the case that a tria and a tessera happen on the
		 * same Vertice in the same move.
		 */
		assertEquals (1, tesseras.size());
		assertEquals (1, trias.size());
	}

	@Test
	public void testDiagnolTria () throws InvalidMoveException {
		alice.setTurn(true);

		GridCell first = new GridCell (9,9,alice.getColor());
		GridCell second = new GridCell (11,11,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		GridCell tessera = new GridCell (10,10, alice.getColor());
		GenteMove move = new GenteMove (alice, tessera);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		Set<BeadString> trias = move.getTrias();
		assertEquals (1, trias.size());
	}
	
	@Test
	public void testInvalidDiagnolTria () throws InvalidMoveException {
		alice.setTurn(true);

		GridCell first = new GridCell (9,9,alice.getColor());
		GridCell second = new GridCell (10,10,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		GridCell tessera = new GridCell (12,12, alice.getColor());
		GenteMove move = new GenteMove (alice, tessera);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		Set<BeadString> trias = move.getTrias();
		assertEquals (0, trias.size());		
	}
	
	@Test
	public void testMixedTessera () throws InvalidMoveException {
		alice.setTurn(true);
		GridCell invalid = new GridCell (8,8, alice.getColor());
		GridCell first = new GridCell (8,9,alice.getColor().getCompliment());
		GridCell second = new GridCell (8,11,alice.getColor());
		
		game.getGrid().updateCell(invalid);
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		game.setState(GameState.PLAY);
		
		GridCell tessera = new GridCell (8,10, alice.getColor());
		GenteMove move = new GenteMove (alice, tessera);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		Set<BeadString> trias = move.getTrias();
		Set<BeadString> tesseras = move.getTesseras();
		assertEquals (1, tesseras.size());
		assertEquals (0, trias.size());		
	}
	
	@Test
	public void testJoinedTrias () throws InvalidMoveException {
		alice.setTurn(true);

		GridCell first = new GridCell (8,10,alice.getColor());
		GridCell second = new GridCell (9,10,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		GridCell third = new GridCell (10,8,alice.getColor());
		GridCell fourth = new GridCell (10,9,alice.getColor());
		
		game.getGrid().updateCell(third);
		game.getGrid().updateCell(fourth);
		
		game.setState(GameState.PLAY);
		
		GridCell tria = new GridCell (10,10, alice.getColor());
		GenteMove move = new GenteMove (alice, tria);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tria, game.getGrid().getLocation(tria));
		
		Set<BeadString> trias = move.getTrias();
		assertEquals (2, trias.size());
	}
	
	@Test
	public void testJoinedTesseras () throws InvalidMoveException {
		alice.setTurn(true);

		GridCell first = new GridCell (7,10,alice.getColor());
		GridCell second = new GridCell (8,10,alice.getColor());
		GridCell third = new GridCell (9,10, alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		game.getGrid().updateCell(third);
		
		GridCell fourth = new GridCell (10,7,alice.getColor());
		GridCell fifth = new GridCell (10,8, alice.getColor());
		GridCell sixth = new GridCell (10,9, alice.getColor());
		
		game.getGrid().updateCell(fourth);
		game.getGrid().updateCell(fifth);
		game.getGrid().updateCell(sixth);
		
		game.setState(GameState.PLAY);
		
		GridCell tess = new GridCell (10,10, alice.getColor());
		GenteMove move = new GenteMove (alice, tess);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tess, game.getGrid().getLocation(tess));
		
		Set<BeadString> tesseras = move.getTesseras();
		assertEquals (2, tesseras.size());
	}

	@Test
	public void testInlineJoinedTesseras () throws InvalidMoveException {
		alice.setTurn(true);

		GridCell first = new GridCell (7,10,alice.getColor());
		GridCell second = new GridCell (8,10,alice.getColor());
		GridCell third = new GridCell (9,10, alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		game.getGrid().updateCell(third);
		
		GridCell fourth = new GridCell (11,10,alice.getColor());
		GridCell fifth = new GridCell (12,10, alice.getColor());
		GridCell sixth = new GridCell (13,10, alice.getColor());
		
		game.getGrid().updateCell(fourth);
		game.getGrid().updateCell(fifth);
		game.getGrid().updateCell(sixth);
		
		game.setState(GameState.PLAY);
		
		GridCell tess = new GridCell (10,10, alice.getColor());
		GenteMove move = new GenteMove (alice, tess);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tess, game.getGrid().getLocation(tess));
		
		Set<BeadString> tesseras = move.getTesseras();
		assertEquals (2, tesseras.size());
	}
	
	
	@Test
	public void testMixedTriaTessera () throws InvalidMoveException {
		alice.setTurn(true);
		GridCell first = new GridCell (10,8,alice.getColor().getCompliment());
		GridCell second = new GridCell (10,9,alice.getColor().getCompliment());
		GridCell third = new GridCell (10,11, alice.getColor().getCompliment());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		game.getGrid().updateCell(third);
		
		game.setState(GameState.PLAY);
		alice.setGame(game);
		
		GridCell tessera = new GridCell (10,10, alice.getColor());
		GenteMove move = new GenteMove (alice, tessera);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tessera, game.getGrid().getLocation(tessera));
		
		Set<BeadString> trias = move.getTrias();
		assertEquals (0, trias.size());
		
		Set<BeadString> tesseras = move.getTesseras();
		assertEquals (1, tesseras.size());
		
	}
	
	public void testTwoUnrelatedTrias () throws InvalidMoveException {
		alice.setTurn(true);

		GridCell first = new GridCell (8,10,alice.getColor());
		GridCell second = new GridCell (9,10,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		game.setState(GameState.PLAY);
		alice.setGame(game);
		
		GridCell tria = new GridCell (10,10, alice.getColor());
		GenteMove move = new GenteMove (alice, tria);
		
		game.move (move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tria, game.getGrid().getLocation(tria));
		
		Set<BeadString> trias = move.getTrias();
		assertEquals (1, trias.size());
		assertEquals (1, alice.getTrias().size());

		first = new GridCell (8,12,alice.getColor());
		second = new GridCell (9,12,alice.getColor());
		
		game.getGrid().updateCell(first);
		game.getGrid().updateCell(second);
		
		/* Clean up turns for our tests */
		Collection<GridPlayer> players = game.getPlayers();
		for (GridPlayer player : players) {
			if (player.getRegistrant().getName().equals(alice.getRegistrant().getName()))
				player.setTurn(true);
			else
				player.setTurn(false);
		}
		
		tria = new GridCell (10,12, alice.getColor());
		move = new GenteMove (alice, tria);
		
		game.move(move);
		
		assertEquals (MoveStatus.EVALUATED, move.getStatus());
		assertEquals (tria, game.getGrid().getLocation(tria));
		
		trias = move.getTrias();
		assertEquals (1, trias.size());
		assertEquals (2, alice.getTrias().size());
		
	}
}
