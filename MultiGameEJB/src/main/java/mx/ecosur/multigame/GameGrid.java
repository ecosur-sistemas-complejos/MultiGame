/**
 * 
 */
package mx.ecosur.multigame;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Embedded;

/**
 * The GameGrid class holds the current state of a specific game.  This class
 * is intended to be a transitive object, in the sense that a specific ShareGameBoard
 * will return a populated GameGrid to a specific caller. This hides gamegrid specific
 * information for runtime callers; allowing quick access to specific cells within a
 * running game.
 * 
 * @author awater
 *
 */
public class GameGrid implements Serializable {

	TreeSet<Cell> cells;
	
	public GameGrid () {
		cells = new TreeSet<Cell>(new CellComparator());
	}

	/**
	 * Initializes a GameGrid object containing the List of Cells.
	 */
	public GameGrid(TreeSet<Cell> cells) {
		this.cells = cells;
	}
	
	/* 
	 * Returns the cell at the location specified by the passed in Cell, null
	 * if no Cell exists at that location.  As we parse only the tail of an
	 * ordered list, this method should return in log(n) time.
	 */
	public Cell getLocation (Cell location) {
		Cell ret = null;
		CellComparator comparator = (CellComparator) cells.comparator();
		SortedSet<Cell> sublist = cells.tailSet(location);
		
		for (Cell c : sublist) {
			int value = comparator.compare(location, c);
			if (value == 0)
				ret = c;
		}
		
		return ret;
	}
	
	public void updateCell (Cell cell) {
		cells.add(cell);
	}
	
	public void removeCell (Cell cell) {
		cells.remove(cell);
	}
	
	@Embedded
	public Set<Cell> getCells () {
		return cells;
	}
	
	public void setCells(Set<Cell> cells){
		this.cells = (TreeSet<Cell>) cells;
	}
	
	public String toString () {
		StringBuffer buf = new StringBuffer();
		Iterator<Cell> iter = cells.iterator();
		buf.append ("Grid: [");
		while (iter.hasNext()) {
			Cell cell = iter.next();
			buf.append("row: " + cell.getRow() + ", column: " + cell.getColumn() +
					", color: " + cell.getColor());
			if (iter.hasNext()) {
				buf.append ("; ");
			}
			else {
				buf.append ("]");
			}
		}
		
		return buf.toString();
	}
	
}
