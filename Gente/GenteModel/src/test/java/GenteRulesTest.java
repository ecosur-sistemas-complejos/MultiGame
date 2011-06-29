/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.DummyMessageSender;
import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.entity.GameGrid;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.impl.entity.gente.*;

import org.junit.Before;
import org.junit.Test;

public class GenteRulesTest extends GenteTestBase {
        
    private GenteGame game;
        
    private GentePlayer alice, bob, charlie, denise;        
        
    @Before
    public void setUp() throws Exception {
        game = new GenteGame();
        game.setMessageSender(new DummyMessageSender());

        GridRegistrant a, b, c, d;
        a = new GridRegistrant ("alice");
        b = new GridRegistrant ("bob");
        c = new GridRegistrant ("charlie");
        d = new GridRegistrant("denise");

        alice = (GentePlayer) game.registerPlayer(a);
        bob = (GentePlayer) game.registerPlayer(b);
        charlie = (GentePlayer) game.registerPlayer(c);
        denise = (GentePlayer) game.registerPlayer(d);
    }

    @Test
    public void testInitialize () {
        assertTrue (game.getGrid().getCells() == null);
        Collection<GridPlayer> players = game.getPlayers();
        GentePlayer p = null;
        for (GridPlayer player : players) {
            if (player.getName().equals("alice")) {
                p = (GentePlayer) player;
                break;
            }
        }

        assertNotNull (p);
        assertEquals ("alice", p.getName());
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
        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(center);

        GridCell next = new GridCell (10,9, alice.getColor());
        GenteMove subsequent = new GenteMove (alice, next);
        game.move (subsequent);
        assertEquals (MoveStatus.EVALUATED, subsequent.getStatus());
    }

    @Test
    public void testExecuteSubsequentMove () throws InvalidMoveException {
        alice.setTurn(true);
        GridCell center = new GridCell (10, 10, alice.getColor());
        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(center);
        game.setState(GameState.PLAY);

        GridCell next = new GridCell (10, 9, alice.getColor());
        GenteMove subsequent = new GenteMove (alice, next);
        game.move(subsequent);
        assertEquals (MoveStatus.EVALUATED, subsequent.getStatus());
        assertEquals (next, game.getGrid().getLocation(next));
    }

    @Test
    public void testFindTheVerticalTrias () throws InvalidMoveException {
        GridCell start = new GridCell (4,3,alice.getColor());
        GridCell second = new GridCell (4,4,alice.getColor());
        GridCell tria = new GridCell (4,5, alice.getColor());

        setIds (start,second,tria);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(start);
        game.getGrid().getCells().add(second);

        GenteMove move = new GenteMove (alice, tria);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tria, game.getGrid().getLocation(tria));

        Set<Tria> set = move.getTrias();
        assertEquals (set.toString(), 1, set.size());
    }

    @Test
    public void testFindCentralVerticalTria () throws InvalidMoveException {
        GridCell start = new GridCell (10,10,alice.getColor());
        GridCell second = new GridCell (10,11,alice.getColor());
        GridCell tria = new GridCell (10,9, alice.getColor());

        setIds (start,second,tria);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(start);
        game.getGrid().getCells().add(second);

        GenteMove move = new GenteMove (alice, tria);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tria, game.getGrid().getLocation(tria));

        Set<Tria> set = move.getTrias();
        assertEquals (1, set.size());
    }

    @Test
    public void testFindTheHorizontalTrias () throws InvalidMoveException {
        GridCell start = new GridCell (3,3,alice.getColor());
        GridCell second = new GridCell (4,3,alice.getColor());
        GridCell tria = new GridCell (5, 3, alice.getColor());

        setIds (start, second, tria);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(start);
        game.getGrid().getCells().add(second);

        GenteMove move = new GenteMove (alice, tria);

        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tria, game.getGrid().getLocation(tria));

        Set<Tria> set = move.getTrias();
        assertEquals (1, set.size());
    }

    @Test
    public void testFindTheTesseras () throws InvalidMoveException {
        alice.setTurn(true);
        GridCell start = new GridCell (4,5,alice.getColor());
        GridCell second = new GridCell (4,4,alice.getColor());
        GridCell third = new GridCell (4,3, alice.getColor().getCompliment());
        GridCell tessera = new GridCell (4,2, alice.getColor());

        setIds (start,second,third,tessera);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(start);
        game.getGrid().getCells().add(second);
        game.getGrid().getCells().add(third);

        GenteMove move = new GenteMove (alice, tessera);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tessera, game.getGrid().getLocation(tessera));

        Set<Tessera> set = move.getTesseras();
        assertEquals (1, set.size());

    }

    @Test
    public void testSelfishScoring () throws InvalidMoveException {
        alice.setTurn(true);
        GridCell first = new GridCell (5,5,alice.getColor());
        GridCell second = new GridCell (5,6,alice.getColor());
        GridCell tria = new GridCell (5,7, alice.getColor());

        GridCell third = new GridCell (3,6, alice.getColor());
        GridCell fourth = new GridCell (3,7, alice.getColor());
        GridCell secondTria = new GridCell(3,8, alice.getColor());

        setIds (first, second, tria, third, fourth, secondTria);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(first);
        game.getGrid().getCells().add(second);
        game.setState(GameState.PLAY);
        GenteMove move = new GenteMove (alice, tria);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tria, game.getGrid().getLocation(tria));
        assertEquals (1, move.getTrias().size());


        game.getGrid ().getCells().add(third);
        game.getGrid ().getCells().add(fourth);
        alice.setTurn(true);
        move = new GenteMove (alice, secondTria);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (secondTria, game.getGrid().getLocation(secondTria));
        assertEquals (1, move.getTrias().size());

        assertEquals (2, alice.getTrias().size());
        assertEquals (10, alice.getPoints());
    }

    @Test
    public void testCooperativeScoring () throws InvalidMoveException {
        alice.setTurn(true);

        /* Setup the first tessera */
        GridCell start = new GridCell (5,4,alice.getColor());
        GridCell second = new GridCell (5,5,alice.getColor().getCompliment());
        GridCell third = new GridCell (5,6, alice.getColor().getCompliment());
        GridCell tessera = new GridCell (5,7, alice.getColor());

        GridCell fourth =  new GridCell (1,3,alice.getColor().getCompliment());
        GridCell fifth =  new GridCell (2,3,alice.getColor().getCompliment());
        GridCell sixth =  new GridCell (3,3,alice.getColor().getCompliment());
        GridCell secondTessera = new GridCell (4,3, alice.getColor());

        GridCell seventh =  new GridCell (8,8,alice.getColor().getCompliment());
        GridCell eighth =  new GridCell (8,7,alice.getColor().getCompliment());
        GridCell ninth =  new GridCell (8,6,alice.getColor().getCompliment());
        GridCell thirdTessera = new GridCell (8,5, alice.getColor());

        setIds (start, second, third, tessera, fourth, fifth, sixth, secondTessera,
                        seventh, eighth, ninth, tessera, thirdTessera);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(start);
        game.getGrid().getCells().add(second);
        game.getGrid().getCells().add(third);
        game.setState(GameState.PLAY);
        GenteMove move = new GenteMove (alice, tessera);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tessera, game.getGrid().getLocation(tessera));

        /* Setup the second tessera */
        game.getGrid().getCells().add(fourth);
        game.getGrid().getCells().add(fifth);
        game.getGrid().getCells().add(sixth);
        alice.setTurn(true);
        move = new GenteMove (alice, secondTessera);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (secondTessera, game.getGrid().getLocation(secondTessera));

        /* Setup the third tessera */
        game.getGrid().getCells().add(seventh);
        game.getGrid().getCells().add(eighth);
        game.getGrid().getCells().add(ninth);
        alice.setTurn(true);
        move = new GenteMove (alice, thirdTessera);
        move = (GenteMove) game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (thirdTessera, game.getGrid().getLocation(thirdTessera));
        assertEquals (5, alice.getPoints());
        assertEquals (3, alice.getTesseras().size ());
        GentePlayer partner = alice.getPartner();
        assertEquals (5, partner.getPoints());
        assertEquals  (3, partner.getTesseras().size());
    }

    @Test
    public void testDiagnolTessera () throws InvalidMoveException {
        GentePlayer partner = alice.getPartner();

        alice.setTurn(true);
        GridCell invalid = new GridCell (8,8, partner.getColor());
        GridCell first = new GridCell (9,9,alice.getColor());
        GridCell second = new GridCell (10,10,partner.getColor());
        GridCell tessera = new GridCell (11,11, alice.getColor());
        setIds (invalid, first, second, tessera);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(invalid);
        game.getGrid().getCells().add(first);
        game.getGrid().getCells().add(second);

        game.setState(GameState.PLAY);

        GenteMove move = new GenteMove (alice, tessera);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tessera, game.getGrid().getLocation(tessera));

        Set<Tessera> tesseras = move.getTesseras();

        assertEquals (1, tesseras.size());
    }

    @Test
    public void testDiagnolTria () throws InvalidMoveException {
        alice.setTurn(true);

        GridCell first = new GridCell (9,9,alice.getColor());
        GridCell second = new GridCell (10,10,alice.getColor());
        GridCell tessera = new GridCell (11,11, alice.getColor());
        setIds (first, second, tessera);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(first);
        game.getGrid().getCells().add(second);

        game.setState(GameState.PLAY);

        GenteMove move = new GenteMove (alice, tessera);
        game.move (move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tessera, game.getGrid().getLocation(tessera));

        Set<Tria> trias = move.getTrias();
        assertEquals (1, trias.size());
}

    @Test
    public void testJoinedTrias () throws InvalidMoveException {
        alice.setTurn(true);

        GridCell first = new GridCell (8,10,alice.getColor());
        GridCell second = new GridCell (9,10,alice.getColor());
        GridCell third = new GridCell (10,8,alice.getColor());
        GridCell fourth = new GridCell (10,9,alice.getColor());
        GridCell tria = new GridCell (10,10, alice.getColor());

        setIds (first, second, third, fourth, tria);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(first);
        game.getGrid().getCells().add(second);
        game.getGrid().getCells().add(third);
        game.getGrid().getCells().add(fourth);
        game.setState(GameState.PLAY);

        GenteMove move = new GenteMove (alice, tria);
        game.move (move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tria, game.getGrid().getLocation(tria));

        Set<Tria> trias = move.getTrias();
        assertEquals (2, trias.size());
    }

    @Test
    public void testInlineJoinedTesseras () throws InvalidMoveException {
        GentePlayer partner = alice.getPartner();

        GridCell first = new GridCell (7,10,alice.getColor());
        GridCell second = new GridCell (8,10,partner.getColor());
        GridCell third = new GridCell (9,10, alice.getColor());

        GridCell fourth = new GridCell (11,10,partner.getColor());
        GridCell fifth = new GridCell (12,10, alice.getColor());
        GridCell sixth = new GridCell (13,10,partner.getColor());
        GridCell tess = new GridCell (10,10, alice.getColor());
        setIds (first, second, third, fourth, fifth, sixth, tess);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(first);
        game.getGrid().getCells().add(second);
        game.getGrid().getCells().add(third);
        game.getGrid().getCells().add(fourth);
        game.getGrid().getCells().add(fifth);
        game.getGrid().getCells().add(sixth);


        /* Tessera move */
        alice.setTurn(true);
        GenteMove move = new GenteMove (alice, tess);
        move = (GenteMove) game.move (move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (tess, game.getGrid().getLocation(tess));

        Set<Tessera> tesseras = move.getTesseras();
        assertEquals (2, tesseras.size());
    }

    @Test
    public void testMalformedTesseras () throws InvalidMoveException, MalformedURLException {

        GridCell first = new GridCell (10, 10, alice.getColor());
        GridCell second = new GridCell (10,11, bob.getColor());
        GridCell third = new GridCell (9,12, charlie.getColor());
        GridCell fourth = new GridCell (8, 12, denise.getColor());
        GridCell fifth = new GridCell (9,9, alice.getColor());
        GridCell sixth = new GridCell (8, 13, bob.getColor());
        GridCell seventh = new GridCell (7,14, charlie.getColor());
        GridCell eighth = new GridCell (6, 15, denise.getColor());
        GridCell ninth = new GridCell (8,8, alice.getColor());
        GridCell tenth= new GridCell (6,14, bob.getColor());
        GridCell eleventh = new GridCell (5,16, charlie.getColor());
        GridCell twelfth = new GridCell (4,17, denise.getColor());

        /* Center cell */
        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(new GridCell(1, 1, Color.UNKNOWN));

        alice.setTurn(true);
        GenteMove move = new GenteMove (alice, first);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (bob,second);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (charlie,third);
        game.move(move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (denise,fourth);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (alice,fifth);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (bob,sixth);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (charlie,seventh);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (denise,eighth);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (alice, ninth);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (1, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (bob,tenth);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (charlie, eleventh);
        game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());

        move = new GenteMove (denise, twelfth);
        move = (GenteMove) game.move (move);
        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals(0, move.getTrias().size());
        assertEquals(0, move.getTesseras().size());
        assertTrue (game.getState().equals(GameState.PLAY));
    }

    @Test
    public void testDiagonalTessera2 () throws Exception {
        GridCell a = new GridCell (8,10, Color.RED);
        GridCell b = new GridCell (9,11,Color.YELLOW);
        GridCell c = new GridCell (10,12,Color.YELLOW);
        GridCell d = new GridCell (11,13,Color.RED);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(a);
        game.getGrid().getCells().add(b);
        game.getGrid().getCells().add(c);

        charlie.setTurn(true);
        GenteMove move = new GenteMove (charlie, d);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (1, move.getTesseras().size());
        assertEquals(0, move.getTrias().size());

    }

    @Test
    public void testDiagonalTessera3 () throws Exception {
        GridCell a = new GridCell (12,13,Color.YELLOW);
        GridCell b = new GridCell (13,12,Color.YELLOW);
        GridCell c = new GridCell (14,11,Color.RED);
        GridCell d = new GridCell (15,10,Color.RED);

        setIds(a,b,c,d);

        GameGrid grid = game.getGrid();
        if (grid.getCells() == null)
            grid.setCells (new TreeSet<GridCell>(new CellComparator()));
        game.getGrid().getCells().add(a);
        game.getGrid().getCells().add(b);
        game.getGrid().getCells().add(c);

        charlie.setTurn(true);
        GenteMove move = new GenteMove (charlie, d);
        game.move(move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (1, move.getTesseras().size());
        assertEquals(0, move.getTrias().size());

    }
}
