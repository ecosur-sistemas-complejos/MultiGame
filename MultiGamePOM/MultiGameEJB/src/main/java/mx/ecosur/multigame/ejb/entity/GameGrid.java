/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * The GameGrid class holds the current state of a specific game.  This class
 * is intended to be a transitive object, in the sense that a specific 
 * SharedBoardwill return a populated GameGrid to a specific caller. This 
 * hides gamegrid specific information for runtime callers; allowing quick
 * access to specific cells within a running game.
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame.ejb.entity;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import mx.ecosur.multigame.CellComparator;


@Entity
public class GameGrid implements Serializable, Cloneable {

	private static final long serialVersionUID = -2579204312184918693L;
	TreeSet<Cell> cells;
	private int id;
	
	public GameGrid () {
		cells = new TreeSet<Cell>(new CellComparator());
	}

	/**
	 * Initializes a GameGrid object containing the List of Cells.
	 */
	public GameGrid(TreeSet<Cell> cells) {
		this.cells = cells;
	}
	
	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/* 
	 * Returns the cell at the location specified by the passed in Cell, null
	 * if no Cell exists at that location.  As we parse only the tail of an
	 * ordered list, this method should return in log(n) time.
	 */
	public Cell getLocation (Cell location) {
		Cell ret = null;
		if (location != null) {
			CellComparator comparator = (CellComparator) cells.comparator();
			SortedSet<Cell> sublist = cells.tailSet(location);
		
			for (Cell c : sublist) {
				int value = comparator.compare(location, c);
				if (value == 0) {
					ret = c;
					break;
				}
			}
		}
		
		return ret;
	}
	
	public void updateCell (Cell cell) {
		if (cells.contains(cell))
			cells.remove(cell);
		cells.add(cell);
	}
	
	public void removeCell (Cell cell) {
		cells.remove(cell);
	}
	
	@OneToMany (cascade={CascadeType.ALL})
	public Set<Cell> getCells () {
		return cells;
	}
	
	public void setCells(Set<Cell> cells){
		this.cells = new TreeSet<Cell> (new CellComparator());
		this.cells.addAll(cells);
	}
	
	public String toString () {
		StringBuffer buf = new StringBuffer();
		Iterator<Cell> iter = cells.iterator();
		buf.append ("GameGrid: [");
		while (iter.hasNext()) {
			Cell cell = iter.next();
			buf.append("column: " + cell.getColumn() + ", row: " + 
					cell.getRow() + ", color: " + cell.getColor());
			if (iter.hasNext()) {
				buf.append ("; ");
			}
			else {
				buf.append ("]");
			}
		}
		
		return buf.toString();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		GameGrid ret = new GameGrid();
		for (Cell cell : cells) {
			Cell cloneCell = cell.clone();
			ret.updateCell(cloneCell);
		}
		
		return ret;
	}
}
