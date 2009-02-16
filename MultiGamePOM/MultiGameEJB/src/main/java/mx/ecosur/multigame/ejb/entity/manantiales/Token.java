/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.entity.manantiales;

import org.apache.commons.lang.builder.HashCodeBuilder;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.manantiales.BorderType;
import mx.ecosur.multigame.manantiales.TokenType;

public class Token extends Cell {
	
	private static final long serialVersionUID = -8048552960014554186L;

	public Token (int column, int row, Color color, TokenType type) {
		super(column, row, color);
		this.type = type;
	}
	
	private TokenType type;
	
	public String getIdentifier() {
		return getColumn() + ":" + getRow();
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	/**
	 * @return the manantial
	 */
	public boolean isManantial() {
		return checkManantial();
	}
	
	/**
	 * @return theScore
	 */
	public int score () {
		return type.value();
	}

	/**
	 * @return the border
	 */
	public BorderType getBorder() {
		return checkBorder();
	}
	
	private BorderType checkBorder () {
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
	private boolean checkManantial() {
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

	public Token clone() throws CloneNotSupportedException {
		Token ret = new Token(this.getColumn(),this.getRow(),this.getColor(),this.getType());
		return ret;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.entity.Cell#toString()
	 */
	@Override
	public String toString() {
		return "[" + super.getColor().toString() + "] " + type.toString() +
			"(" + getColumn() + "," + getRow() + ")";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		
		
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
}
