/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.ejb.entity.manantiales;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang.builder.HashCodeBuilder;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.impl.manantiales.BorderType;
import mx.ecosur.multigame.impl.manantiales.TokenType;
import mx.ecosur.multigame.model.Cell;

@Entity
public class Ficha extends Cell {

	private static final long serialVersionUID = -8048552960014554186L;
	private TokenType type;
	
	public Ficha () {
		super(); 
	}

	public Ficha (int column, int row, Color color, TokenType type) {
		super(column, row, color);
		this.type = type;
	}

	@Enumerated (EnumType.STRING)
	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}
	
	/**
	 * @return theScore
	 */
	public int score () {
		int ret = 0;
		if (type != null)
			ret = type.value();
		return ret;
	}
	
	public BorderType getBorder () {
		BorderType ret = BorderType.NONE;
		if (this.getRow() == 4) {
			if (this.getColumn() < 4) 
				ret = BorderType.WEST;
			else if (this.getColumn() > 4)
				ret = BorderType.EAST;
		} else if (this.getColumn() == 4) {
			if (this.getRow () < 4)
				ret = BorderType.NORTH;
			else if (this.getRow () > 4)
				ret = BorderType.SOUTH;
		}
		
		return ret;
	}

	/**
	 * @return
	 */
	public boolean isManantial() {
		if (type.equals(TokenType.UNDEVELOPED))
			return false;
		
		/* Check all other tokens */
		boolean ret = false;
		if (this.getColumn() == 3 && this.getRow() == 4)
			ret = true;
		else if (this.getColumn() == 4  && this.getRow() == 3)
			ret = true;
		else if (this.getColumn() == 5 && this.getRow() == 4)
			ret = true;
		else if (this.getColumn() == 4 && this.getRow() == 5)
			ret = true;
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof Ficha) {
			Ficha comp = (Ficha) obj;
			ret = (comp.type.equals(this.type) &&
					comp.getRow() == getRow() &&
					comp.getColumn() == getColumn() &&
					comp.getColor() == getColor());
		} 
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder() 
        .append(getColumn()) 
        .append(getRow())
        .append(getType())
        .append(getColor())
        .toHashCode(); 
	}
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Cell#toString()
	 */
	@Override
	public String toString() {
		return "[Ficha, type = " + type + ", " + super.toString();		
	}	
}
