/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Compares two cells for equality by row and subsequently, column (colors of 
 * cells are ignored).
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame.impl;

import java.io.Serializable;
import java.util.Comparator;

import mx.ecosur.multigame.model.implementation.CellImpl;

public class CellComparator implements Comparator<CellImpl>, Serializable {

	private static final long serialVersionUID = 1192095593909731426L;

	public int compare(CellImpl cell1, CellImpl cell2) {
		int ret = -1;
		
		/* First, compare rows */
		if (cell1.getRow () > cell2.getRow())
			ret = 1;
		else if (cell1.getRow() < cell2.getRow())
			ret = -1;
		else if (cell1.getRow() == cell2.getRow())
			ret = 0;
		
		/* Next, compare columns, if rows are equal) */
		if (ret == 0) {
			if (cell1.getColumn() > cell2.getColumn())
				ret = 1;
			else if (cell1.getColumn() < cell2.getColumn())
				ret = -1;
			else if (cell1.getColumn() == cell2.getColumn()) 
				ret = 0;
		}
		
		return ret;
	}

}
