/*
* Copyright (C) 2008-2011 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * GenteMove extends Move to add some Pente specific methods for use by the 
 * Pente/Gente rules.  
 * 
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.gente;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.util.BeadString;
import mx.ecosur.multigame.grid.entity.GridMove;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.grid.entity.GridCell;


@Entity
public class GenteMove extends GridMove {

    private static final long serialVersionUID = -6635578671376146204L;

    public enum CooperationQualifier {
        COOPERATIVE, SELFISH, NEUTRAL
    }

    private Set<Tria> trias;

    private Set<Tessera> tesseras;

    private CooperationQualifier qualifier = null;

    private ArrayList<Color> teamColors;

    public GenteMove () {
        super ();
    }

    /**
     * @param player
     * @param cell
     */
    public GenteMove(GridPlayer player, GridCell cell) {
        super (player, cell);
    }

    public void addTria(BeadString string) throws Exception {
        Tria t = new Tria();
        t.setCells(string.getBeads());
        if (trias == null)
            trias = new HashSet<Tria>();
        trias.add(t);
    }

    public void addTrias (BeadString... trias) throws Exception {
        for (BeadString tria : trias)
            addTria(tria);
    }

    public void addTessera (BeadString tessera) {
        Tessera t = new Tessera();
        t.setCells(tessera.getBeads());
        if (tesseras == null)
            tesseras = new HashSet<Tessera>();
        tesseras.add(t);
    }

    public void addTesseras (BeadString... tesseras) {
        for (BeadString tessera : tesseras) {
            addTessera(tessera);
        }
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

    @Enumerated(EnumType.STRING)
    public CooperationQualifier getQualifier() {
        return qualifier;
    }

    public void setQualifier(CooperationQualifier qualifier) {
        this.qualifier = qualifier;
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

    @Transient
    public ArrayList<Color> getTeamColors () {
        if (teamColors == null) {
            teamColors = new ArrayList<Color>();
            GentePlayer player = (GentePlayer) this.player;
            teamColors.add (player.getPartner().getColor());
            teamColors.add(this.player.getColor());
        }

        return teamColors;
    }

    @Override
    protected Object clone() {
       GenteMove ret = new GenteMove ();
        try {
            if (current != null)
                ret.current = current.clone();
            if (destination != null)
                ret.destination = destination.clone();
            if (player != null) {
                GentePlayer p = (GentePlayer) this.player;
                ret.player = (GridPlayer) p.clone();
            }
            ret.qualifier = this.qualifier;

            ret.teamColors = new ArrayList<Color>();
            if (tesseras != null) {
                ret.tesseras = new HashSet<Tessera>();
                for (Tessera t : tesseras) {
                    ret.tesseras.add(t);
                }
            }

            if (trias != null) {
                ret.trias = new HashSet<Tria>();
                for (Tria tria : trias) {
                    ret.trias.add(tria);
                }
            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /* (non-Javadoc)
     * @see GridMove#toString()
     */
    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("GenteMove: ");
        ret.append("Player=" + getPlayer() + ", ");
        ret.append("current=" + getCurrentCell() + ", ");
        ret.append("destination=" + getDestinationCell() + ", ");
        ret.append("qualifier=" + qualifier + ", ");
        ret.append("trias=" + trias + ", ");
        ret.append("tesseras=" + tesseras + ", ");
        return ret.toString();
    }

    @Override
    public int hashCode() {
       int curCode = 1, destCode = 1;
       if (current != null)
        curCode = curCode - current.hashCode();
       if (destination != null)
         destCode = destCode + destination.hashCode();
       return 31 * curCode + destCode;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof GenteMove) {
            GenteMove comparison = (GenteMove) obj;
            if (current != null && destination !=null) {
                ret = current.equals( (comparison.getCurrentCell())) &&
                      destination.equals(comparison.getDestinationCell());
            } else if (destination != null) {
                ret = destination.equals(comparison.getDestinationCell());
              }
        }

        return ret;
    }
}

