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

@SuppressWarnings("serial")
public class Move implements Model, Serializable {
	
	private MoveImpl moveImpl;
	
	public Move (MoveImpl moveImpl) {
		this.moveImpl = moveImpl;
	}

	/**
	 * @return
	 */
	public GamePlayer getPlayer() {
		return new GamePlayer (moveImpl.getPlayer());
	}

	/**
	 * @param player
	 */
	public void setPlayer(GamePlayer player) {
		moveImpl.setPlayer (player.getImplementation());
	}

	/**
	 * @return
	 */
	public Cell getCurrent() {
		return new Cell(moveImpl.getCurrent());
	}

	/**
	 * @param find
	 */
	public void setCurrent(Cell cell) {
		moveImpl.setCurrent (cell.getImplementation());
	}

	/**
	 * @return
	 */
	public MoveStatus getStatus() {
		return moveImpl.getStatus();
	}
	
	public MoveImpl getImplementation() {
		return moveImpl;
	}
}
