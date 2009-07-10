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

import java.io.Serializable;

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.model.implementation.MoveImpl;

public class Move implements Model, Serializable {
	
	private static final long serialVersionUID = 8462209469604832617L;
	
	private MoveImpl moveImpl;
	
	public Move (MoveImpl moveImpl) {
		this.moveImpl = moveImpl;
	}

	/**
	 * @return
	 */
	public MoveStatus getStatus() {
		return moveImpl.getStatus();
	}
	
	public GamePlayer getPlayer () {
		return moveImpl.getPlayerModel();
	}
	
	public void setPlayer (GamePlayer player) {
		moveImpl.setPlayerModel (player);
	}
	
	public MoveImpl getImplementation() {
		return moveImpl;
	}
}
