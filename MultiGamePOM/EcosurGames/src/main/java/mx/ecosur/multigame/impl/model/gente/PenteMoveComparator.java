/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.model.gente;

import java.io.Serializable;
import java.util.Comparator;

import mx.ecosur.multigame.impl.CellComparator;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;
import mx.ecosur.multigame.impl.model.GridCell;

public class PenteMoveComparator implements Comparator<GenteMove>, Serializable {
	
	private static final long serialVersionUID = -2506716322565071451L;

	/**
	 * Compares the destinations of two moves based upon
	 * the CellCompartor logic.
	 * 
	 * @param move1, move2
	 */
	public int compare(GenteMove move1, GenteMove move2) {
		GridCell cell1 = (GridCell) move1.getDestination();
		GridCell cell2 = (GridCell) move2.getDestination();
		CellComparator comparator = new CellComparator();
		return comparator.compare(cell1, cell2);
	}

}
