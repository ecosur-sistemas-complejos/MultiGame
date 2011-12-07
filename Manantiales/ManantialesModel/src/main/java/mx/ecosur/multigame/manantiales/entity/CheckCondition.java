/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.manantiales.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.manantiales.enums.ConditionType;

import mx.ecosur.multigame.model.interfaces.Condition;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
public class CheckCondition implements Condition {
    
    private static final long serialVersionUID = -9183594100309734640L;

    private ConditionType reason;

    private GridPlayer agent;

    private Set<ManantialesFicha> violators;

    private boolean expired;

    private boolean resolved;
    
    private Integer id;

    public CheckCondition () {
        super();
    }

    public CheckCondition (ConditionType reason, GridPlayer agent, ManantialesFicha...violator)
    {
        this.reason = reason;
        this.agent = agent;
        this.violators = new HashSet<ManantialesFicha>();
        for (ManantialesFicha cell : violator) {
            this.violators.add(cell);
        }
    }

    /**
     * @return the id
     */
    @Id @GeneratedValue
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }


    /**
     * @return the player
     */
    @ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    public GridPlayer getPlayer() {
        return agent;
    }

    /**
     * @param player the player to set
     */
    public void setPlayer (GridPlayer player) {
        this.agent = player;
    }

    /**
     * @return the reason
     */
    @Transient
    public String getReason() {
        return reason.toString();
    }

    @Enumerated(EnumType.STRING)
    public ConditionType getType () {
        return reason;
    }

    public void setType(ConditionType type) {
        this.reason = type;
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
     * @return the expired
     */
    public boolean isResolved() {
        return expired;
    }

    /**
     * @param expired the expired to set
     */
    public void setResolved(boolean expired) {
        this.expired = expired;
    }    

    /**
     * @return the violators
     */
    @ManyToMany (cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    public Set<ManantialesFicha> getViolators() {
        if (violators == null)
            violators = new HashSet<ManantialesFicha>();
        return violators;
    }

    /**
     * @param violators the violators to set
     */
    public void setViolators(Set<ManantialesFicha> violators) {
        this.violators = violators;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().
            append(getId()).
            append(getPlayer()).
            append(getReason()).
            append(getType()).
            toHashCode();
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
            ret = ret && (test.getPlayer().equals(this.getPlayer()));
            ret = ret && (test.getViolators().equals(this.getViolators()));
        }

        return ret;
    }
}
