/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * GentePlayer extends GridRegistrant to provide several Pente specific methods
 * for use by the Pente rule set.
 * 
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.gente;

import javax.persistence.Entity;

import mx.ecosur.multigame.grid.model.GridRegistrant;
import mx.ecosur.multigame.grid.util.BeadString;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.model.GridPlayer;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashSet;

@Entity
public class GentePlayer extends GridPlayer {
        
    private static final long serialVersionUID = -7540174337729169503L;

    private int points;

    private HashSet<BeadString> trias, tesseras;

    private GentePlayer partner;

    public GentePlayer () {
            super ();
            points = 0;
    }

    /**
     * @param player
     * @param color
     */
    public GentePlayer(GridRegistrant player, Color color) {
        super (player, color);
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public HashSet<BeadString> getTrias() {
        if (trias == null)
            trias = new HashSet<BeadString>();
        return trias;
    }

    public void setTrias(HashSet<BeadString> trias) {
        this.trias = trias;
    }

    public void addTria (BeadString tria) {
        if (trias == null) {
            trias = new HashSet<BeadString> ();
        }

        if (tria.size() == 3)
            trias.add (tria);
    }

    public HashSet<BeadString> getTesseras() {
        if (tesseras == null)
            tesseras = new HashSet<BeadString>();
        return tesseras;
    }

    public void setTesseras(HashSet<BeadString> tesseras) {
        this.tesseras = tesseras;
    }

    public void addTessera (BeadString tessera) {
        if (tesseras == null) {
            tesseras = new HashSet<BeadString> ();
        }

        if (tessera.size() == 4)
            tesseras.add(tessera);
    }

    public GentePlayer getPartner () {
        return partner;
    }

    public void setPartner (GentePlayer partner) {
        this.partner = partner;
    }


    public boolean containsString (BeadString comparison) {
        boolean ret = false;

        for (BeadString string : getTrias()) {
            if (comparison.contains(string))
                ret = true;
        }

        for (BeadString string : getTesseras()) {
            if (comparison.contains(string))
                ret = true;
        }

        return ret;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        GentePlayer ret = new GentePlayer ();
        ret.setId(getId());
        ret.setPoints(getPoints());
        HashSet<BeadString> clones = new HashSet<BeadString>();
        for (BeadString tessera : getTesseras()) {
            BeadString clone = (BeadString) tessera.clone();
            clones.add(clone);
        }
        ret.setTesseras(clones);

        clones = new HashSet<BeadString>();
        for (BeadString tria : getTrias()) {
            BeadString clone = (BeadString) tria.clone();
            clones.add(clone);
        }
        ret.setTrias(clones);
        ret.setColor(getColor());

        GridRegistrant clone = (GridRegistrant) getRegistrant().clone();
        ret.setRegistrant(clone);
        ret.setTurn(isTurn());
        ret.setPoints(getPoints());

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = super.equals(obj);
        if (obj instanceof GentePlayer) {
            GentePlayer comp = (GentePlayer) obj;
            ret = ret && comp.points == this.points;
        }

        return ret;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).append(points).append(tesseras).append(trias).append(getColor()).toHashCode();
    }
}
