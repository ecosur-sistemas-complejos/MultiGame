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

import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.GamePlayerImpl;
import mx.ecosur.multigame.model.implementation.RegistrantImpl;

public class GamePlayer implements Model {
	
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

	public Game getGame() {
		GameImpl game = implementation.getGame();
		return new Game(game);
	}

	public Registrant getPlayer() {
		RegistrantImpl registrant = implementation.getPlayer();
		return new Registrant (registrant);
	}	

}
