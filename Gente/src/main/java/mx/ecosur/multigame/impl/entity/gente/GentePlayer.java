/*
* Copyright (C) 2010,2011 ECOSUR, Andrew Waterman and Max Pimm
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

import javax.persistence.*;

import mx.ecosur.multigame.grid.util.BeadString;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
public class GentePlayer extends GridPlayer {
        
    private static final long serialVersionUID = -7540174337729169503L;

    private int points;

    private Set<Tria> trias;

    private Set<Tessera> tesseras;

    private GentePlayer partner;

    public GentePlayer () {
        super ();
        points = 0;
        partner = null;
    }

    /**
     * @param player
     * @param color
     */
    public GentePlayer(GridRegistrant player, Color color) {
        super (player, color);
    }

    public void addTria(BeadString string) throws Exception {
        Tria t = new Tria();
        t.setCells(string.getBeads());
        getTrias().add(t);
    }

    public void addTessera (BeadString string) {
        Tessera t = new Tessera();
        t.setCells(string.getBeads());
        getTesseras().add(t);
    }


    public boolean containsString (BeadString comparison) {
        boolean ret = false;
        if (comparison.size() == 3) {
            if (trias != null) {
                Tria comp = new Tria();
                try {
                    comp.setCells(comparison.getBeads());
                    for (Tria t : trias) {
                        ret = t.equals(comp);
                        if (ret)
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (comparison.size() == 4) {
            if (tesseras != null) {
                Tessera comp = new Tessera();
                comp.setCells(comparison.getBeads());
                for (Tessera t : tesseras) {
                    ret = comp.equals(t);
                    if (ret)
                        break;
                }
            }
        }

        return ret;
    }

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)
    public GentePlayer getPartner() {
        return partner;
    }

    public void setPartner(GentePlayer partner) {
        this.partner = partner;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)
    public Set<Tessera> getTesseras() {
        if (tesseras == null)
            tesseras = new HashSet<Tessera>();
        return tesseras;
    }

    public void setTesseras(Set<Tessera> tesseras) {
        this.tesseras = tesseras;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)
    public Set<Tria> getTrias() {
        if (trias == null)
            trias = new HashSet<Tria>();
        return trias;
    }

    public void setTrias(Set<Tria> trias) {
        this.trias = trias;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        GentePlayer ret = new GentePlayer ();
        ret.setId(getId());
        ret.points = points;
        ret.tesseras = tesseras;
        ret.trias = trias;
        ret.setColor(getColor());

        GridRegistrant clone = (GridRegistrant) getRegistrant().clone();
        ret.setRegistrant(clone);
        ret.setTurn(isTurn());
        ret.points = points;

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof GentePlayer) {
            GentePlayer comp = (GentePlayer) obj;
            if (comp.getRegistrant().getName().equals(this.getRegistrant().getName()))
                ret = true;
        }

        return ret;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).append(points).append(tesseras).append(trias).append(getColor()).toHashCode();
    }
}
