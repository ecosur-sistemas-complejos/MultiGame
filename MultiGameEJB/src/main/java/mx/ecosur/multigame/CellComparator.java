/**
 * 
 */
package mx.ecosur.multigame;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares two cells for equality by row and column (colors are ignored).
 * 
 * @author awater
 *
 */
public class CellComparator implements Comparator<Cell>, Serializable {

	public int compare(Cell cell1, Cell cell2) {
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
