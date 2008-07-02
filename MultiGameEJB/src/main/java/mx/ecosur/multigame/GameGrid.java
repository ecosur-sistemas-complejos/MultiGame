/**
 * 
 */
package mx.ecosur.multigame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

	List<Cell> cells;
	
	public GameGrid () {
		cells = new ArrayList<Cell>();
	}

	/**
	 * Initializes a GameGrid object containing the List of Cells.
	 */
	public GameGrid(List<Cell> cells) {
		this.cells = cells;
	}
	
	
	/*
	 * Returns the Cell at the specified location
	 */
	public Cell getLocation (int x, int y) {
		Cell ret = null;
		
		/* Search all cells in the list for the requested co-ordinates */
		/* TODO Update this search to be more efficient */
		ListIterator<Cell> iter = cells.listIterator();
		while (iter.hasNext()) {
			Cell candidate = iter.next();
			if (candidate.getRow() == x && candidate.getColumn() == y) {
				ret = candidate;
				break;
			}
		}
		
		return ret;
		
	}
	
	public Cell getLocation (Cell location) {
		return getLocation(location.getRow(), location.getColumn());
	}
	
	public void updateCell (Cell cell) {
		Cell existing = getLocation(cell.getRow(), cell.getColumn());
		if (existing != null)
			cells.remove(existing);
		cells.add(cell);
	}
	
	public void removeCell (Cell cell) {
		cells.remove(cell);
	}
	
	@Embedded
	public List<Cell> getCells () {
		return cells;
	}
	
	public void setCells(List<Cell> cells){
		this.cells = cells;
	}
	
	public String toString () {
		StringBuffer buf = new StringBuffer();
		ListIterator<Cell> iter = cells.listIterator();
		buf.append ("Grid: [");
		while (iter.hasNext()) {
			Cell cell = iter.next();
			buf.append("row: " + cell.getRow() + ", column: " + cell.getColumn() +
					", color: " + cell.getColor());
			if (iter.hasNext()) {
				buf.append (",");
			}
			else {
				buf.append ("]");
			}
		}
		
		return buf.toString();
	}
}
