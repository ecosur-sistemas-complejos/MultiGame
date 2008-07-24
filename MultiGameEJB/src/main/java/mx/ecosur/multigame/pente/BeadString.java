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
}
