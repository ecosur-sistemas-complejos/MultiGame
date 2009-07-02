/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.solver.manantiales;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.entity.manantiales.Ficha;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;

public class SolverFicha extends Ficha {
	
	private int counter = 0;

	/**
	 * @param col
	 * @param row
	 * @param color
	 * @param undeveloped
	 */
	public SolverFicha(int col, int row, Color color, TokenType type) {
		super (col, row, color, type);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Cell#getId()
	 */
	@Override
	public int getId() {
		return counter++;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.manantiales.Ficha#clone()
	 */
	@Override
	public SolverFicha clone() throws CloneNotSupportedException {
		return new SolverFicha(this.getColumn(),this.getRow(), 
				this.getColor(),this.getType());
	}
}
