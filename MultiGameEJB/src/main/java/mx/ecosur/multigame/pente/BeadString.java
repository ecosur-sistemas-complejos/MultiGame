/**
 * 
 */
package mx.ecosur.multigame.pente;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mx.ecosur.multigame.Cell;

/**
 * @author awater
 *
 */
public class BeadString implements Serializable {
	
	private List<Cell> beads;
	
	public BeadString () {
		this.beads = new ArrayList<Cell>();
	}

	public BeadString (List<Cell> beads) {
		this.beads = beads;
	}
	
	public List<Cell> getBeads () {
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

}
