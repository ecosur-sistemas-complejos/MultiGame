/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * A GridRegistrant contains persistent information about a player playing a 
 * specific game.  
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame.grid.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.model.interfaces.GamePlayer;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class GridPlayer implements GamePlayer {

    private static final long serialVersionUID = -1893870933080422147L;

    private int id;

    private GridRegistrant player;

    private Color color;

    private boolean turn;

    public static String getNamedQuery () {
        return "getGamePlayer";
    }

    public GridPlayer () {
        super();
    }

    public GridPlayer (GridRegistrant player, Color color) {
        this.player = player;
        this.color = color;
    }

    @Id
    @GeneratedValue
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GridRegistrant getRegistrant() {
        return player;
    }

    public void setRegistrant (GridRegistrant player) {
        this.player = player;
    }

    @Enumerated (EnumType.STRING)
    public Color getColor() {
        return color;
    }

    public void setColor (Color color) {
        this.color = color;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    @Override
    public String toString() {
        return player.getName() + ", " + color.name() + ", turn=" + turn;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;

        /* Simple equality test based upon stated "name" of associated registrant */
        if (obj instanceof GridPlayer) {
            GridPlayer test = (GridPlayer) obj;
            if (test.getRegistrant().getName().equals(this.getRegistrant().getName()))
                ret = true;
        }

        return ret;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(color).append(player).append(turn).toHashCode();
    }

}
