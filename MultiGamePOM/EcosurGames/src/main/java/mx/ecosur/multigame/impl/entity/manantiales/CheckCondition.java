/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.manantiales;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import mx.ecosur.multigame.impl.enums.manantiales.ConditionType;
import mx.ecosur.multigame.impl.model.GridPlayer;

import mx.ecosur.multigame.model.implementation.ConditionImpl;

@Entity
public class CheckCondition implements ConditionImpl {
	
	private static final long serialVersionUID = -9183594100309734640L;
	
	ConditionType reason;
	GridPlayer agent;
	HashSet<Ficha> violators;
	private boolean expired;
	private int id;	
	
	public CheckCondition () {
		reason = null;
		agent = null;
		violators = null;
		expired = false;
	}
	
	public CheckCondition (ConditionType reason, GridPlayer agent, 
			Ficha...violator)  
	{
		this.reason = reason;
		this.agent = agent;
		this.violators = new HashSet<Ficha>();
		for (Ficha cell : violator) {
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
	public GridPlayer getPlayer() {
		return agent;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer (GridPlayer player) {
		this.agent = agent;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason.toString();
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = ConditionType.valueOf(reason);
	}
	
	@Transient
	public ConditionType getType () {
		return reason;
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
	@OneToMany (cascade={CascadeType.ALL},
			fetch=FetchType.EAGER)	
	public Set<Ficha> getViolators() {
		return violators;
	}

	/**
	 * @param violators the violators to set
	 */
	public void setViolators(Set<Ficha> violators) {
		this.violators = (HashSet<Ficha>) violators;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.ConditionImpl#getTriggers()
	 */
    @Transient
	public Object[] getTriggers() {
		return violators.toArray();
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
}
