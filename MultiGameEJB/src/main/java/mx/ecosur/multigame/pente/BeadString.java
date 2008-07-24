/**
 * 
 */
package mx.ecosur.multigame.pente;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Vertice;

/**
 * @author awater
 *
 */
public class BeadString implements Serializable {
	
	private TreeSet<Cell> beads;
	
	public BeadString () {
		this.beads = new TreeSet<Cell>(new CellComparator());
	}
	
	public Set<Cell> getBeads () {
		return beads;
	}
	
	public void setBeads(Set<Cell> beads){
		this.beads = (TreeSet<Cell>)beads;
	}
	
	public void add (Cell cell) {
		beads.add(cell);
	}
	
	public int size () {
		return beads.size();
	}
	
	public boolean contains (Cell cell) {
		return beads.contains(cell);
	}
	
	public boolean contains (BeadString string) {
		boolean ret = false;
		int count = 0;
		
		for (Cell cell : string.getBeads()) {
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
	 * Verifies that a given beadstring is contiguous.
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
		StringBuffer buf = new StringBuffer ();
		for (Cell cell : beads) {
			buf.append(cell.toString());
			buf.append (" ");
		}
		
		return buf.toString();
	}
}
