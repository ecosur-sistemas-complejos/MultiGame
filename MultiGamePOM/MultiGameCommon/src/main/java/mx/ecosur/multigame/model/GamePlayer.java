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

public class GamePlayer implements Model {
	
	private static final long serialVersionUID = 8222591967075020143L;
	
	private GamePlayerImpl implementation;

	public GamePlayer (GamePlayerImpl gamePlayerImpl) {
		this.implementation = gamePlayerImpl;
	}
	
	public int getId() {
		return implementation.getId();
	}	
	
	public GamePlayerImpl getImplementation() {
		return implementation;
	}
	
	public boolean isTurn () {
		return implementation.isTurn();
	}
	
	public void setTurn (boolean bool) {
		implementation.setTurn(bool);
	}	
}
