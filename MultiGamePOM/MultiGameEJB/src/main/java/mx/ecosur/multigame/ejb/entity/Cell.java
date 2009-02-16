/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * A cell represents a location on a gameboard. Cells have a color, specified 
 * by means of the Color enum.
 * 
 * @author awaterma@ecosur.mx
 * 
 */

package mx.ecosur.multigame.ejb.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;

@Entity
public class Cell implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7917786935353131901L;

	private int row, column;

	private Color color;

	private CellComparator comparator;

	private int id;

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
		this.comparator = new CellComparator();
	}
	
	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column (name="CELL_ROW")
	public int getRow() {
		return row;
	}

	public void setRow(int y) {
		this.row = y;
	}

	@Column (name="CELL_COLUMN")
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

	public Cell clone() throws CloneNotSupportedException {
		Cell clone = new Cell(this.column, this.row, Color
				.valueOf(color.name()));
		return clone;
	}

	public String toString() {
		return "(Column, Row) Column = " + column + ", Row = " + row + 
			", Color = " + color;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		
		if (obj instanceof Cell) {
			Cell comparison = (Cell) obj;
			if (comparator.compare(this, comparison) == 0 && 
					comparison.getColor().equals(this.getColor()))
					ret = true;
		} else 
			ret = super.equals(obj);
		
		return ret;
	}
}
