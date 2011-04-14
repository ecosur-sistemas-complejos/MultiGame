/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
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

import javax.persistence.*;

import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.impl.enums.manantiales.ConditionType;

import mx.ecosur.multigame.model.interfaces.Condition;

<<<<<<< HEAD:Manantiales/ManantialesModel/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
@Entity
=======
@Entity ()
>>>>>>> Fixes for Manantiales. Sourced OptimsticLockException down to CheckConditions Set in ManantialesGame.  With this getter marked @Transient, games can now be loaded and played with out the exception.  Will correct on return from semana santa.:Manantiales/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
public class CheckCondition implements Condition {
    
    private static final long serialVersionUID = -9183594100309734640L;

<<<<<<< HEAD:Manantiales/ManantialesModel/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
    private ConditionType reason;

    private GridPlayer agent;

    private Set<ManantialesFicha> violators;

=======
    ConditionType reason;
    GridPlayer agent;
    Set<ManantialesFicha> violators;
>>>>>>> Fixes for Manantiales. Sourced OptimsticLockException down to CheckConditions Set in ManantialesGame.  With this getter marked @Transient, games can now be loaded and played with out the exception.  Will correct on return from semana santa.:Manantiales/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
    private boolean expired;

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
<<<<<<< HEAD:Manantiales/ManantialesModel/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
    @ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
=======
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
>>>>>>> Fixes for Manantiales. Sourced OptimsticLockException down to CheckConditions Set in ManantialesGame.  With this getter marked @Transient, games can now be loaded and played with out the exception.  Will correct on return from semana santa.:Manantiales/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
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
<<<<<<< HEAD:Manantiales/ManantialesModel/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
    @Transient
=======
>>>>>>> Fixes for Manantiales. Sourced OptimsticLockException down to CheckConditions Set in ManantialesGame.  With this getter marked @Transient, games can now be loaded and played with out the exception.  Will correct on return from semana santa.:Manantiales/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
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
     * @return the violators
     */
<<<<<<< HEAD:Manantiales/ManantialesModel/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
    @ManyToMany (cascade=CascadeType.ALL, fetch=FetchType.EAGER)
=======
    @OneToMany (cascade=CascadeType.ALL, fetch=FetchType.EAGER)
>>>>>>> Fixes for Manantiales. Sourced OptimsticLockException down to CheckConditions Set in ManantialesGame.  With this getter marked @Transient, games can now be loaded and played with out the exception.  Will correct on return from semana santa.:Manantiales/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
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
<<<<<<< HEAD:Manantiales/ManantialesModel/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
=======
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.model.interfaces.Condition#getTriggers()
     */
    @Transient
    public Object[] getTriggers() {
        return violators.toArray();
>>>>>>> Fixes for Manantiales. Sourced OptimsticLockException down to CheckConditions Set in ManantialesGame.  With this getter marked @Transient, games can now be loaded and played with out the exception.  Will correct on return from semana santa.:Manantiales/src/main/java/mx/ecosur/multigame/impl/entity/manantiales/CheckCondition.java
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
