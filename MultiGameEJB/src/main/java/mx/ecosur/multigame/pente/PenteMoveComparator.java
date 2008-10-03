/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.pente;

import java.io.Serializable;
import java.util.Comparator;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;

public class PenteMoveComparator implements Comparator<PenteMove>, Serializable {
	
	private static final long serialVersionUID = -2506716322565071451L;

	/**
	 * Compares the destinations of two moves based upon
	 * the CellCompartor logic.
	 * 
	 * @param move1, move2
	 */
	public int compare(PenteMove move1, PenteMove move2) {
		Cell cell1 = move1.getDestination();
		Cell cell2 = move2.getDestination();
		CellComparator comparator = new CellComparator();
		return comparator.compare(cell1, cell2);
	}

}
