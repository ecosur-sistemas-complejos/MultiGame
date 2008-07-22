/**
 * 
 */
package mx.ecosur.multigame;

import java.io.Serializable;

/**
 * A cell represents a location on a gameboard. Cells are colored, with the
 * coloration set as specified in the Color enum.
 * 
 * @author awater
 * 
 */
public class Cell implements Serializable, Cloneable {

	private int row, column;

	private Color color;

	private Characteristic characteristic;

	private CellComparator comparator;

	public Cell() {
		this.comparator = new CellComparator();
	}

	/*
	 * Instantiates a new Cell, at the named co-ordinates, set to the specified
	 * Color
	 */
	public Cell(int x, int y, Color color) {
		this.column = x;
		this.row = y;
		this.color = color;
		this.characteristic = null;
		this.comparator = new CellComparator();
	}

	public int getRow() {
		return row;
	}

	public void setRow(int y) {
		this.row = y;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int x) {
		this.column = x;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Characteristic getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(Characteristic characteristic) {
		this.characteristic = characteristic;
	}

	public Cell clone() throws CloneNotSupportedException {
		Cell clone = new Cell(this.column, this.row, Color
				.valueOf(color.name()));
		if (this.characteristic != null)
			clone.characteristic = characteristic.clone();
		return clone;
	}

	public String toString() {
		return "(Column, Row) Column = " + column + ", Row = " + row + ", Color = " + color
				+ ", Characteristic = " + characteristic;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		
		if (obj instanceof Cell) {
			Cell comparison = (Cell) obj;
			if (comparator.compare(this, comparison) == 0)
				ret = true;
		} else 
			ret = super.equals(obj);
		
		return ret;
	}
}
