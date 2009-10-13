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

import mx.ecosur.multigame.model.implementation.GamePlayerImpl;
import mx.ecosur.multigame.model.implementation.Implementation;

public class GamePlayer implements Model {
	
	private static final long serialVersionUID = 8222591967075020143L;
	
	private GamePlayerImpl implementation;
	
	public GamePlayer () {
		super();
	}

	public GamePlayer (GamePlayerImpl gamePlayerImpl) {
		this.implementation = gamePlayerImpl;
	}
	
	public int getId() {
		return implementation.getId();
	}
	
	public GamePlayerImpl getImplementation() {
		return implementation;
	}	
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Model#setImplementation(mx.ecosur.multigame.model.implementation.Implementation)
	 */
	public void setImplementation(Implementation impl) {
		this.implementation = (GamePlayerImpl) impl;
	}
}
