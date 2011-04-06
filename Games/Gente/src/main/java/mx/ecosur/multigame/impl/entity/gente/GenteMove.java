/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
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
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.util.BeadString;
import mx.ecosur.multigame.grid.model.GridMove;
import mx.ecosur.multigame.grid.model.GridPlayer;
import mx.ecosur.multigame.grid.model.GridCell;


@Entity
public class GenteMove extends GridMove {
        
        private static final long serialVersionUID = -6635578671376146204L;

        public enum CooperationQualifier {
            COOPERATIVE, SELFISH, NEUTRAL
        }
        
        private Set<BeadString> trias, tesseras;

        private CooperationQualifier qualifier = null;
        
        private ArrayList<Color> teamColors;
        
        public GenteMove () {
            super ();
            player = null;
        }
        
        /**
         * @param player
         * @param cell
         */
        public GenteMove(GridPlayer player, GridCell cell) {
            super (player, cell);
        }
        
        public void addTria(BeadString t) {
            getTrias().add(t);
        }

        public void addTrias (BeadString... trias) {
            for (BeadString tria : trias)
                getTrias().add(tria);
        }

        /**
         * Gets the Trias that this move created.
         */
        @Transient
        public Set<BeadString> getTrias () {
            if (trias == null)
                trias = new HashSet<BeadString>();
            return trias;
        }
        
        public void setTrias (Set<BeadString> new_trias) {
            trias = new_trias;
        }
        
        public void addTessera (BeadString t) {
            getTesseras().add(t);
        }

        public void addTesseras (BeadString... tesseras) {
            for (BeadString tessera : tesseras) {
                getTesseras().add(tessera);
            }
        }

        /**
         * Gets the Tesseras that this move created.
         */
        @Transient
        public Set<BeadString> getTesseras () {
            if (tesseras == null)
                tesseras = new HashSet<BeadString>();
            return tesseras;
        }

        public void setTesseras (Set<BeadString> new_tesseras) {
            tesseras = new_tesseras;
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

        public void setTeamColors (ArrayList<Color> colors) {
            teamColors = colors;
        }
        
        /**
         * Gets the qualifier
         * 
         * @return the qualifier
         */
        @Enumerated(EnumType.STRING)
        public CooperationQualifier getQualifier() {
            if (qualifier == null)
                qualifier = CooperationQualifier.NEUTRAL;
            return qualifier;
        }

        /**
         * Sets the cooperation qualifier
         * 
         * @param qualifier
         *            the cooperation qualifier
         */
        public void setQualifier(CooperationQualifier qualifier) {
            this.qualifier = qualifier;
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
            ret.append("qualifier=" + getQualifier() + ", ");
            ret.append("trias=" + getTrias().size() + ", ");
            ret.append("tesseras=" + getTesseras().size() + ", ");
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
                    ret.tesseras = new LinkedHashSet<BeadString>();
                    for (BeadString string : getTesseras()) {
                        ret.tesseras.add((BeadString) string.clone());
                    }
                }

                if (trias != null) {
                    ret.trias = new LinkedHashSet<BeadString>();
                    for (BeadString string : getTrias()) {
                        ret.trias.add((BeadString) string.clone());
                    }
                }

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            return ret;
        }
}

