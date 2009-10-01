/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model;

import mx.ecosur.multigame.model.implementation.CellImpl;
import mx.ecosur.multigame.model.implementation.Implementation;

public class Cell implements Model {
	
	private static final long serialVersionUID = -5382384031755001844L;
	
	private CellImpl cellImpl;
	
	public Cell () {
		super();
	}
	
	public Cell (CellImpl cellImpl) {
		this.cellImpl = cellImpl;
	}

	/**
	 * @return
	 */
	public int getId() {
		return cellImpl.getId();
	}

    public void setId (int id) {
        cellImpl.setId (id);
    }

	/**
	 * @return
	 */
	public int getRow() {
		return cellImpl.getRow();
	}

    public void setRow (int row) {
        cellImpl.setRow(row);
    }

	/**
	 * @return
	 */
	public int getColumn() {
		return cellImpl.getColumn();
	}

    public void setColumn (int column) {
        cellImpl.setColumn (column);
    }

	/**
	 * @return
	 */
	public CellImpl getImplementation() {
		return cellImpl;
	}

	public void setImplementation(Implementation impl) {
		this.cellImpl = (CellImpl) impl;
	}
}
