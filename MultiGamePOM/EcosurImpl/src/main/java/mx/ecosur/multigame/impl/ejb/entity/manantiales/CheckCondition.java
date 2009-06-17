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

import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import mx.ecosur.multigame.Condition;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.impl.manantiales.ConditionType;

@SuppressWarnings("serial")
@Entity
public class CheckCondition implements Condition {

	ConditionType reason;
	GamePlayer player;
	HashSet<Cell> violators;
	private boolean expired;
	private int id;
	
	public CheckCondition () {
		super();
	}	
	
	public CheckCondition (ConditionType reason, GamePlayer player, 
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
	 * @return the id
	 */
	@Id @GeneratedValue
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
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
	public ConditionType getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(ConditionType reason) {
		this.reason = reason;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = true;
		if (obj instanceof CheckCondition) {
			CheckCondition test = (CheckCondition) obj;
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
