/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.util.gente;

import java.io.Serializable;
import java.util.Comparator;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.model.GridCell;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;

public class GenteMoveComparator implements Comparator<GenteMove>, Serializable {
        
    private static final long serialVersionUID = -2506716322565071451L;

    public int compare(GenteMove move1, GenteMove move2) {
        GridCell cell1 = (GridCell) move1.getDestinationCell();
        GridCell cell2 = (GridCell) move2.getDestinationCell();
        CellComparator comparator = new CellComparator();
        return comparator.compare(cell1, cell2);
    }
}
