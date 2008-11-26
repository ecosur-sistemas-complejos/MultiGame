/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * A BeadString contains a small set of tokens, called a "string" in Pente, and
 * allows clients to perform some basic operations upon each string.  For use
 * in the Pente/Gente rule sets.
 * 
 *	@author awaterma@ecosur.mx 
 */

package mx.ecosur.multigame.pente;

import java.io.Serializable;
import java.util.TreeSet;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.util.Direction;
import mx.ecosur.multigame.util.Vertice;

public class BeadString implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5360218565926616845L;
	private TreeSet<Cell> beads;
	
	public BeadString () {
		this.beads = new TreeSet<Cell>(new CellComparator());
	}

	public TreeSet<Cell> getBeads () {
		return beads;
	}
	
	public void setBeads(TreeSet<Cell> new_beads){
		beads.addAll(new_beads);
	}
	
	public void add (Cell cell) {
		beads.add(cell);
	}
	
	public boolean remove (Cell cell) {
		return beads.remove(cell);
	}
	
	public int size () {
		return beads.size();
	}
	
	public boolean contains (Cell cell) {
		return beads.contains(cell);
	}
	
	public boolean isTerminator (Cell cell) {
		return (beads.first() == cell || beads.last() == cell);
	}
	
	public boolean contains (BeadString string) {
		boolean ret = false;
		int count = 0;
		
		for (Cell cell : beads) {
			if (string.contains(cell)) 
				count++;
			if (count > 1) { 
				ret = true;
				break;
			}
		}
		
		return ret;
	}
	
	/**
	 * Returns the Direction to which these beads point.
	 * @return
	 */
	public Direction getDirection () {
		Direction ret = Direction.UNKNOWN;
		
		/* Calculate the slope */
		int x = beads.first().getColumn() - beads.last().getColumn();
		int y = beads.first().getRow() - beads.last().getRow();
		
		/** TODO: Determine NE,SE,NW,SW directions */
		if (x == 0 && y == 0) {
			if (beads.first().getRow() > beads.last().getRow())
				ret = Direction.NORTH;
			else
				ret = Direction.SOUTH;
		} else {
			float slope = x / y;
			if (slope == 0) {
				if (beads.first().getColumn() > beads.last().getColumn()) 
					ret = Direction.EAST;
				else
					ret = Direction.WEST;
			} 
		}
		
		return ret;
	}
	
	/**
	 * Verifies that a given beadstring is contiguous on a given Vertice.
	 * @return
	 */
	
	public boolean contiguous (Vertice v) {
		boolean ret = true;
		
		int[] slope = new int [ 2 ];
		
		switch (v) {
			case HORIZONTAL:
				slope [ 0 ] = 1;
				slope [ 1 ] = 0;
				break;
			case VERTICAL:
				slope [ 0 ] = 0;
				slope [ 1 ] = 1;
				break;
			case FORWARD:
				slope [ 0 ] = -1;
				slope [ 1 ] = 1;
				break;
			case REVERSE:
				slope [ 0 ] = 1;
				slope [ 1 ] = 1;
				break;
			}
		
		Cell lastCell = beads.first();
		for (Cell cell : beads.tailSet(beads.first())) {
			/* Skip the first cell */
			if (cell.equals(lastCell))
				continue;
			if (cell.getColumn() == lastCell.getColumn() + slope [ 0 ] &&
					cell.getRow() == lastCell.getRow () + slope [ 1 ]) {
				lastCell = cell;
			} else {
				ret = false;
				break;
			}
		}
		
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof BeadString){
			BeadString comparison = (BeadString) obj;
			ret = beads.equals(comparison.beads);
		} else
			ret = super.equals(obj);
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer ("BeadString [");
		for (Cell cell : beads) {
			buf.append(cell.toString());
			buf.append (" ");
		}
		buf.append (" ]");
		return buf.toString();
	}

	public BeadString trim(Cell destination, int stringlength) {
		BeadString ret = new BeadString();
		if (beads.first() == destination) {
			ret.setBeads(new TreeSet<Cell> (beads.tailSet(destination)));			
		} else if (beads.last() == destination) {
			ret.setBeads(new TreeSet<Cell>(beads.headSet(destination)));
		}
		if (!ret.contains(destination))
			ret.add(destination);
		return ret;
	}
}
