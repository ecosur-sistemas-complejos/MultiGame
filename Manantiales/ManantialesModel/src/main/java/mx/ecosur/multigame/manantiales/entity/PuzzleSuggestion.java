/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

package mx.ecosur.multigame.manantiales.entity;

import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.model.interfaces.GamePlayer;
import mx.ecosur.multigame.model.interfaces.Move;
import mx.ecosur.multigame.model.interfaces.Suggestion;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * @author awaterma@ecosur.mx
 */
@Entity
public class PuzzleSuggestion implements Suggestion {

    private static final long serialVersionUID = -1649014283342618955L;

    private ManantialesMove move;

    private ManantialesPlayer suggestor;

    private SuggestionStatus status;

    private int id;

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

    public Move listMove() {
        return getMove();
    }

    public void attachMove(Move move) {
        setMove ((ManantialesMove) move);
    }

    @ManyToOne (cascade = CascadeType.ALL)
    public ManantialesMove getMove() {
        return move;
    }

    public void setMove(ManantialesMove move) {
        this.move = move;
    }

    public void attachSuggestor(GamePlayer gamePlayer) {
        setSuggestor ((ManantialesPlayer) gamePlayer);
    }

    public GamePlayer listSuggestor() {
        return getSuggestor();
    }

    @ManyToOne (cascade = CascadeType.ALL)
    public ManantialesPlayer getSuggestor() {
        return suggestor;
    }

    public void setSuggestor (ManantialesPlayer suggestor) {
        this.suggestor = suggestor;
    }

    @Enumerated(EnumType.STRING)
    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Suggestion [status = [" + status + "], suggestor = [" +
                suggestor + "], move = [" + move + "]";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().
            append(getId()).
            append(getStatus()).
            append(getMove()).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret;

        if (obj instanceof PuzzleSuggestion) {
            PuzzleSuggestion comparison = (PuzzleSuggestion) obj;

            ret = (this.getSuggestor().equals(comparison.getSuggestor()))
                    && (this.getMove()).equals(comparison.getMove());
            if (comparison.getId() > 0) {
                ret = ret && this.getId() == comparison.getId();
            }

        } else {
            ret = super.equals(obj);
        }
        
        return ret;
    }
}
