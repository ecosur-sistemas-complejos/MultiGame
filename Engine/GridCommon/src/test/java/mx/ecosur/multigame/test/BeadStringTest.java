/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

package mx.ecosur.multigame.test;

import static junit.framework.Assert.*;

import mx.ecosur.multigame.grid.enums.Vertice;
import mx.ecosur.multigame.grid.model.BeadString;
import mx.ecosur.multigame.grid.model.GridCell;
import mx.ecosur.multigame.grid.Color;
import org.junit.Test;

public class BeadStringTest {

    @Test
    public void testHorzontalContiguous () {

        GridCell a = new GridCell(5,5, Color.UNKNOWN);
        GridCell b = new GridCell(6,5, Color.UNKNOWN);
        GridCell c = new GridCell(7,5, Color.UNKNOWN);
        BeadString test = new BeadString(a,b,c);
        assertTrue (test.isContiguous(Vertice.VERTICAL));

        GridCell d = new GridCell (8,5, Color.UNKNOWN);
        test = new BeadString (a,b,d);
        assertFalse (test.isContiguous(Vertice.VERTICAL));
    }

    @Test
    public void testVerticalContiguous () {
        GridCell a = new GridCell(5,5, Color.UNKNOWN);
        GridCell b = new GridCell(5,6, Color.UNKNOWN);
        GridCell c = new GridCell(5,7, Color.UNKNOWN);
        BeadString test = new BeadString(a,b,c);
        assertTrue (test.isContiguous(Vertice.HORIZONTAL));

        GridCell d = new GridCell (5,8, Color.UNKNOWN);
        test = new BeadString (a,b,d);
        assertFalse (test.isContiguous(Vertice.HORIZONTAL));
    }

    @Test
    public void testReverseContiguous () {
        GridCell a = new GridCell(5,5, Color.UNKNOWN);
        GridCell b = new GridCell(6,4, Color.UNKNOWN);
        GridCell c = new GridCell(7,3, Color.UNKNOWN);
        BeadString test = new BeadString(a,b,c);
        assertTrue (test.isContiguous(Vertice.REVERSE));

        GridCell d = new GridCell (8,8, Color.UNKNOWN);
        test = new BeadString (a,b,d);
        assertFalse (test.isContiguous(Vertice.REVERSE));
    }

    @Test
    public void testForwardContiguous () {
        GridCell a = new GridCell(5,5, Color.UNKNOWN);
        GridCell b = new GridCell(6,6, Color.UNKNOWN);
        GridCell c = new GridCell(7,7, Color.UNKNOWN);
        BeadString test = new BeadString(a,b,c);
        assertTrue ("Not contiguous!", test.isContiguous(Vertice.FORWARD));

        GridCell d = new GridCell (8,2, Color.UNKNOWN);
        test = new BeadString (a,b,d);
        assertFalse (test.isContiguous(Vertice.FORWARD));
    }

    @Test
    public void testForwardContiguous2 () {
        GridCell a = new GridCell(3,7, Color.UNKNOWN);
        GridCell b = new GridCell(4,8, Color.UNKNOWN);
        GridCell c = new GridCell(5,9, Color.UNKNOWN);
        BeadString test = new BeadString(a,b,c);
        assertTrue ("Not contiguous!", test.isContiguous(Vertice.FORWARD));

        GridCell d = new GridCell (8,2, Color.UNKNOWN);
        test = new BeadString (a,b,d);
        assertFalse (test.isContiguous(Vertice.FORWARD));
    }
}                                     
