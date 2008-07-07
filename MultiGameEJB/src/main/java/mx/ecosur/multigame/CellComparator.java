/**
 * 
 */
package mx.ecosur.multigame;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares two cells for equality by computing the sum of the two
 * cells rows and columns, and then comparing that sum between
 * the two cells under examination.
 * 
 * @author awater
 *
 */
public class CellComparator implements Comparator<Cell>, Serializable {

	public int compare(Cell cell1, Cell cell2) {
		int ret = -1;
		
		Color col1 = cell1.getColor();
		Color col2 = cell2.getColor();
		
		if (col1.equals(col2)) {	
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
			
		} else {
			ret = col1.compareTo(col2);
		}
		
		return ret;
	}

}
