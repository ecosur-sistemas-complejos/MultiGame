/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

import static junit.framework.Assert.*;
import static mx.ecosur.multigame.impl.util.gente.RuleFunctions.*;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.util.BeadString;
import mx.ecosur.multigame.grid.model.GridCell;
import mx.ecosur.multigame.impl.event.gente.MoveEvent;
import org.junit.Test;

import java.util.Set;

public class GenteRuleFunctionTest extends GenteTestBase {

    @Test
    public void testPermutationsFromCenter () {

        GridCell first = new GridCell (10,7, Color.RED);
        GridCell second = new GridCell (10,8, Color.RED);
        GridCell third = new GridCell (10,9, Color.YELLOW);

        GridCell center = new GridCell(10,10, Color.YELLOW);

        GridCell fifth = new GridCell (10,11, Color.RED);
        GridCell sixth = new GridCell (10,12, Color.RED);
        GridCell seventh = new GridCell (10,13, Color.YELLOW);

        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(center);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(seventh);

        Set<MoveEvent> permutations = getPermutations (center, plane, 4);

        /* MoveEvent equals and hashcode method should be updated */
        assertEquals (2, permutations.size());

    }

    @Test
    public void testPermutationsFromBeginning () {
        GridCell origin = new GridCell(10,6, Color.YELLOW);

        GridCell first = new GridCell (10,7, Color.RED);
        GridCell second = new GridCell (10,8, Color.RED);
        GridCell third = new GridCell (10,9, Color.YELLOW);
        GridCell fourth = new GridCell (10,10, Color.RED);
        GridCell fifth = new GridCell (10,11, Color.RED);
        GridCell sixth = new GridCell (10,12, Color.YELLOW);


        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(fourth);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(origin);

        Set<MoveEvent> permutations = getPermutations (origin, plane, 4);

        assertEquals (1, permutations.size());

    }

    @Test
    public void testPermutationsFromSecondPosition () {
        GridCell origin = new GridCell(10,7, Color.YELLOW);

        GridCell first = new GridCell (10,6, Color.RED);
        GridCell second = new GridCell (10,8, Color.RED);
        GridCell third = new GridCell (10,9, Color.YELLOW);
        GridCell fourth = new GridCell (10,10, Color.RED);
        GridCell fifth = new GridCell (10,11, Color.RED);
        GridCell sixth = new GridCell (10,12, Color.YELLOW);


        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(fourth);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(origin);

        Set<MoveEvent> permutations = getPermutations (origin, plane, 4);

        assertEquals (1, permutations.size());
    }

    @Test
    public void testPermutationsFromThirdPosition () {
        GridCell origin = new GridCell(10,8, Color.YELLOW);

        GridCell first = new GridCell (10,6, Color.RED);
        GridCell second = new GridCell (10,7, Color.RED);
        GridCell third = new GridCell (10,9, Color.YELLOW);
        GridCell fourth = new GridCell (10,10, Color.RED);
        GridCell fifth = new GridCell (10,11, Color.RED);
        GridCell sixth = new GridCell (10,12, Color.YELLOW);


        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(fourth);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(origin);

        Set<MoveEvent> permutations = getPermutations (origin, plane, 4);

        assertEquals (1, permutations.size());
    }


    @Test
    public void testPermutationsFromEnd () {
        GridCell first = new GridCell (10,7, Color.RED);
        GridCell second = new GridCell (10,8, Color.RED);
        GridCell third = new GridCell (10,9, Color.YELLOW);
        GridCell fourth = new GridCell (10,10, Color.RED);
        GridCell fifth = new GridCell (10,11, Color.RED);
        GridCell sixth = new GridCell (10,12, Color.YELLOW);
        GridCell end = new GridCell(10,13, Color.YELLOW);

        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(fourth);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(end);

        Set<MoveEvent> permutations = getPermutations (end, plane, 4);

        assertEquals (1, permutations.size());
    }    

    @Test
    public void testPermutationsFromSecondToEnd () {
        GridCell origin= new GridCell (10,12,Color.YELLOW);

        GridCell first = new GridCell (10,7, Color.RED);
        GridCell second = new GridCell (10,8, Color.RED);
        GridCell third = new GridCell (10,9, Color.YELLOW);
        GridCell fourth = new GridCell (10,10, Color.RED);
        GridCell fifth = new GridCell (10,11, Color.RED);
        GridCell end = new GridCell(10,13, Color.YELLOW);

        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(fourth);
        plane.add(fifth);
        plane.add(end);
        plane.add(origin);

        Set<MoveEvent> permutations = getPermutations (end, plane, 4);

        assertEquals (1, permutations.size());

    }

    @Test
    public void testPermutationsFromThirdToEnd() {
        GridCell origin= new GridCell (10,11,Color.YELLOW);

        GridCell first = new GridCell (10,7, Color.RED);
        GridCell second = new GridCell (10,8, Color.RED);
        GridCell third = new GridCell (10,9, Color.YELLOW);
        GridCell fourth = new GridCell (10,10, Color.RED);
        GridCell fifth = new GridCell (10,12, Color.RED);
        GridCell end = new GridCell(10,13, Color.YELLOW);

        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(fourth);
        plane.add(fifth);
        plane.add(end);
        plane.add(origin);

        Set<MoveEvent> permutations = getPermutations (end, plane, 4);

        assertEquals (1, permutations.size());
    }

    @Test
    public void testPermutationsFromCenterForwardDiagonal () {

        GridCell first = new GridCell (10,7, Color.RED);
        GridCell second = new GridCell (11,8, Color.RED);
        GridCell third = new GridCell (12,9, Color.YELLOW);

        GridCell center = new GridCell(13,10, Color.YELLOW);

        GridCell fifth = new GridCell (14,11, Color.RED);
        GridCell sixth = new GridCell (15,12, Color.RED);
        GridCell seventh = new GridCell (16,13, Color.YELLOW);

        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(center);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(seventh);

        Set<MoveEvent> permutations = getPermutations (center, plane, 4);

        /* MoveEvent equals and hashcode method should be updated */
        assertEquals (2, permutations.size());

    }

    @Test
    public void testPermutationsFromCenterReverseDiagonal () {

        GridCell first = new GridCell (10,10, Color.RED);
        GridCell second = new GridCell (11,9, Color.RED);
        GridCell third = new GridCell (12,8, Color.YELLOW);

        GridCell center = new GridCell(13,7, Color.YELLOW);

        GridCell fifth = new GridCell (14,6, Color.RED);
        GridCell sixth = new GridCell (15,5, Color.RED);
        GridCell seventh = new GridCell (16,4, Color.YELLOW);

        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(center);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(seventh);

        Set<MoveEvent> permutations = getPermutations (center, plane, 4);

        /* MoveEvent equals and hashcode method should be updated */
        assertEquals (2, permutations.size());

    }

    @Test
    public void testPermutationsWithNoise () {
        GridCell first = new GridCell (10,10, Color.BLUE);
        GridCell second = new GridCell (10,9, Color.GREEN);
        GridCell third = new GridCell (10,8, Color.BLUE);
        GridCell center = new GridCell(10,7, Color.YELLOW);

        GridCell fifth = new GridCell (10,6, Color.RED);
        GridCell sixth = new GridCell (10,5, Color.RED);
        GridCell seventh = new GridCell (10,4, Color.YELLOW);

        BeadString plane = new BeadString();
        plane.add (first);
        plane.add(second);
        plane.add(third);
        plane.add(center);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(seventh);

        Set<MoveEvent> permutations = getPermutations (center, plane, 4);

        /* MoveEvent equals and hashcode method should be updated */
        assertEquals (2, permutations.size());
    }

    @Test
    public void testPermutationsWithNoiseForwardDiagonal () {
        /*
        GridCell first = new GridCell (10,7, Color.GREEN);
        GridCell second = new GridCell (11,8, Color.BLUE);
        GridCell third = new GridCell (12,9, Color.GREEN);
        */

        GridCell center = new GridCell(13,10, Color.YELLOW);

        GridCell fifth = new GridCell (14,11, Color.RED);
        GridCell sixth = new GridCell (15,12, Color.RED);
        GridCell seventh = new GridCell (16,13, Color.YELLOW);

        BeadString plane = new BeadString();
        /*
        plane.add (first);
        plane.add(second);
        plane.add(third);
        */
        plane.add(center);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(seventh);

        Set<MoveEvent> permutations = getPermutations (center, plane, 4);

        /* MoveEvent equals and hashcode method should be updated */
        assertEquals (1, permutations.size());
    }

    @Test
    public void testPermutationswithNoiseReverseDiagonal () {
        /*
        GridCell first = new GridCell (10,10, Color.BLUE);
        GridCell second = new GridCell (11,9, Color.GREEN);
        GridCell third = new GridCell (12,8, Color.GREEN);
        */

        GridCell center = new GridCell(13,7, Color.YELLOW);

        GridCell fifth = new GridCell (14,6, Color.RED);
        GridCell sixth = new GridCell (15,5, Color.RED);
        GridCell seventh = new GridCell (16,4, Color.YELLOW);

        BeadString plane = new BeadString();
        /*
        plane.add (first);
        plane.add(second);
        plane.add(third);
        */
        plane.add(center);
        plane.add(fifth);
        plane.add(sixth);
        plane.add(seventh);

        Set<MoveEvent> permutations = getPermutations (center, plane, 4);

        /* MoveEvent equals and hashcode method should be updated */
        assertEquals (1, permutations.size());
        
    }
}
