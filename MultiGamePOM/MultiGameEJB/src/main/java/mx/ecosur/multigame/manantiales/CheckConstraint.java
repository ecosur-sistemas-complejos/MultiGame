/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.manantiales;

import java.io.Serializable;
import java.util.HashSet;

import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.GamePlayer;

@SuppressWarnings("serial")
public class CheckConstraint implements Serializable {
	
	CheckConstraintType reason;
	GamePlayer player;
	HashSet<Cell> violators;
	private boolean expired;
	
	public CheckConstraint () {
		super();
	}	
	
	public CheckConstraint (CheckConstraintType reason, GamePlayer player, 
			Cell...violator) 
	{
		super();
		this.reason = reason;
		this.player = player;
		this.violators = new HashSet<Cell>();
		for (Cell cell : violator) {
			this.violators.add(cell);
		}		
	}

	/**
	 * @return the player
	 */
	public GamePlayer getPlayer() {
		return player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(GamePlayer player) {
		this.player = player;
	}

	/**
	 * @return the reason
	 */
	public CheckConstraintType getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(CheckConstraintType reason) {
		this.reason = reason;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = true;
		if (obj instanceof CheckConstraint) {
			CheckConstraint test = (CheckConstraint) obj;
			ret = ret && (test.getReason().equals(this.getReason()));				
		}
		
		return ret;
	}

	/**
	 * @return the expired
	 */
	public boolean isExpired() {
		return expired;
	}

	/**
	 * @param expired the expired to set
	 */
	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	/**
	 * @return the violators
	 */
	public HashSet<Cell> getViolators() {
		return violators;
	}

	/**
	 * @param violators the violators to set
	 */
	public void setViolators(HashSet<Cell> violators) {
		this.violators = violators;
	}
}
