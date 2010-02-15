/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.impl.enums.manantiales.SuggestionStatus;
import mx.ecosur.multigame.model.implementation.ConditionImpl;

import javax.persistence.*;

/**
 * @author awaterma@ecosur.mx
 */
@Entity
public class Suggestion implements ConditionImpl {

    private ManantialesMove move;

    private ManantialesPlayer suggestor;

    private SuggestionStatus status;

    @OneToOne
    public ManantialesMove getMove() {
        return move;
    }

    public void setMove(ManantialesMove move) {
        this.move = move;
    }

    @OneToOne
    public ManantialesPlayer getSuggestor() {
        return suggestor;
    }

    public void setSuggestor(ManantialesPlayer suggestor) {
        this.suggestor = suggestor;
    }

    @Enumerated(EnumType.STRING)
    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }

    @Transient
    public String getReason() {
        return status.toString();
    }

    public void setReason (String reason) {
        status = SuggestionStatus.valueOf(reason);
    }

    @Transient
    public Object[] getTriggers() {
        return new Object[] { suggestor, move }; 
    }

    public void setTriggers (Object[] triggers) {
        if (triggers.length != 2) { throw new RuntimeException ("Invalid trigger length!"); }
        suggestor = (ManantialesPlayer) triggers [  0 ];
        move = (ManantialesMove) triggers [ 1 ];
    }
}
