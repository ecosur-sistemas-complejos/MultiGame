/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * A BeadString contains a small set of tokens, called a "string" in Gente, and
 * allows clients to perform some basic operations upon each string.  For use
 * in the Gente rule sets.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.grid.model;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.enums.Direction;
import mx.ecosur.multigame.grid.enums.Vertice;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Sort;

import javax.persistence.*;

public class BeadString implements Serializable, Cloneable {
        
    private static final long serialVersionUID = -5360218565926616845L;

    private Set<GridCell> beads;

    private int id;

    public BeadString () {
        super();
    }

    public BeadString (GridCell... cells) {
        this();
        for (GridCell cell : cells) {
            add(cell);
        }
    }


    public Set<GridCell> getBeads () {
        if (beads == null)
            beads = new TreeSet<GridCell>(new CellComparator());
        return beads;
    }

    public void setBeads(Set<GridCell> new_beads){
        beads = new_beads;
    }

    public void add (GridCell cell) {
        getBeads().add(cell);
    }

    public boolean remove (GridCell cell) {
        boolean ret = false;
        if (beads != null)
            ret = beads.remove(cell);
        return ret;
    }

    public int size () {
        int ret = 0;
        if (beads != null)
            ret = beads.size();
        return ret;
    }

    public boolean contains (GridCell cell) {
        boolean ret = false;
        if (beads != null)
            ret = beads.contains(cell);
        return ret;
    }

    public boolean isTerminator (GridCell cell) {
        boolean ret = false;
        if (beads != null) {
            TreeSet<GridCell> inner = new TreeSet<GridCell>(new CellComparator());
            inner.addAll(beads);
            ret = (inner.first() == cell || inner.last() == cell);
        }

        return ret;
    }

    public boolean contains (BeadString string) {
        boolean ret = false;

        if (beads != null) {
            int count = 0;
            for (GridCell cell : beads) {
                if (string.contains(cell))
                    count++;
            }

            if (count >= 2)
                ret = true;
        }

        return ret;
    }

    /**
     * Returns the Direction to which these beads point.
     * @return
     */
    public Direction findDirection() {
        Direction ret = Direction.UNKNOWN;
        if (beads != null) {
            TreeSet<GridCell> inner = new TreeSet<GridCell>(new CellComparator());
            inner.addAll(beads);

            /* Calculate the slope */
            int x = inner.first().getColumn() - inner.last().getColumn();
            int y = inner.first().getRow() - inner.last().getRow();

            /** TODO: Determine NE,SE,NW,SW directions */
            if (x == 0 && y == 0) {
                if (inner.first().getRow() > inner.last().getRow())
                        ret = Direction.NORTH;
                else
                        ret = Direction.SOUTH;
            } else {
                float slope = (float) x / y;
                if (slope == 0) {
                    if (inner.first().getColumn() > inner.last().getColumn())
                            ret = Direction.EAST;
                    else
                            ret = Direction.WEST;
                }
            }
        }

        return ret;
    }

    /**
     * Verifies that a given beadstring is contiguous on a given Vertice.
     * @return boolean
     */
    public boolean isContiguous(Vertice v) {
        boolean ret = true;

        int horizontal = 0, vertical = 0;

        switch (v) {
            case HORIZONTAL:
                    horizontal = 1;
                    vertical   = 0;
                    break;
            case VERTICAL:
                    horizontal = 0;
                    vertical   = 1;
                    break;
            case FORWARD:
                    horizontal = 1;
                    vertical   = 1;
                    break;
            case REVERSE:
                    horizontal = 1;
                    vertical   = -1;
                    break;
            default:
                throw new RuntimeException ("Vertice not set!");
        }

        if (beads != null) {
            TreeSet<GridCell> inner = new TreeSet<GridCell>(new CellComparator());
            inner.addAll(beads);
            GridCell lastCell = inner.first();
            for (GridCell cell : inner.tailSet(inner.first())) {
                if (cell.equals(lastCell))
                        continue;
                if (cell.getColumn() == lastCell.getColumn() + vertical &&
                        cell.getRow() == lastCell.getRow () + horizontal) {
                    lastCell = cell;
                } else {
                    ret = false;
                    break;
                }
            }
        }

        return ret;
    }

    public BeadString trim(GridCell destination, int stringlength) {
        BeadString ret = new BeadString();
        if (beads != null) {
            TreeSet<GridCell> inner = new TreeSet<GridCell>(new CellComparator());
            inner.addAll(beads);
            if (inner.first() == destination) {
                    ret.setBeads(new TreeSet<GridCell> (inner.tailSet(destination)));
            } else if (inner.last() == destination) {
                    ret.setBeads(new TreeSet<GridCell>(inner.headSet(destination)));
            }

            if (!ret.contains(destination))
                ret.add(destination);
        }
        return ret;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        BeadString ret = new BeadString ();
        for (GridCell cell : beads) {
            ret.add((GridCell) cell.clone());
        }

        return ret;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer ("BeadString [");
        if (beads != null) {
            for (GridCell cell : beads) {
                buf.append(cell.toString());
                buf.append (" ");
            }
        }
        buf.append (" ]");
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof BeadString){
            BeadString comparison = (BeadString) obj;
            if (beads != null)
                ret = beads.equals(comparison.beads);
        } else
            ret = super.equals(obj);
        return ret;
    }

    @Override
    public int hashCode() {
        if (beads != null)
            return new HashCodeBuilder().append(beads).hashCode();
        else
            return super.hashCode();
    }
}
