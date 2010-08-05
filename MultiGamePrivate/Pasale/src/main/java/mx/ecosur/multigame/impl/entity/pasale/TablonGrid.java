/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.pasale;

import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.CellComparator;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;

import javax.persistence.Transient;
import java.util.TreeSet;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Nov 18, 2009
 * Time: 11:35:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class TablonGrid extends GameGrid {
    /**
     *
     * Gets the sqaure of cells centered on this ficha. Note these are the 4 cells that
     * immediately surround the passed in Ficha. So, in the case of a particle (water
     * or soil) it will return what exists of the surrounding forest.  For
     * forest it will return the surrounding particles.
     *
     * @param ficha
     * @return
     */
    @Transient
    public SortedSet<TablonFicha> getSquare (TablonFicha ficha) {
        SortedSet<TablonFicha> ret = new TreeSet<TablonFicha>(new CellComparator());


        // x - 1, y -1
        if ( this.getLocation (new GridCell (
                ficha.getColumn() - 1, ficha.getRow() - 1, Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() - 1, ficha.getRow() - 1, Color.UNKNOWN)));
        }

        // x + 1, y - 1
        if ( this.getLocation (new GridCell (
                ficha.getColumn() - 1, ficha.getRow() + 1, Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() - 1, ficha.getRow() + 1, Color.UNKNOWN)));
        }

        // x - 1, y +1
        if ( this.getLocation (new GridCell (
                ficha.getColumn() + 1, ficha.getRow() - 1, Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() + 1, ficha.getRow() - 1, Color.UNKNOWN)));
        }

        // x + 1, y + 1
        if ( this.getLocation (new GridCell (
                ficha.getColumn() + 1, ficha.getRow() + 1, Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() + 1, ficha.getRow() + 1, Color.UNKNOWN)));
        }

        return ret;
    }

    /**
     *
     * Retreives the octogon of similar cells surrounding a given ficha.  Hence, all
     * cells within 2 steps (X and Y) are gathered into the return set.  
     *
     * @param ficha
     * @return
     */

    @Transient
    public SortedSet<TablonFicha> getOctogon (TablonFicha ficha) {
        SortedSet<TablonFicha> ret = new TreeSet<TablonFicha>(new CellComparator());

        if ( this.getLocation (new GridCell (
                ficha.getColumn() - 2, ficha.getRow() - 2, Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() - 2, ficha.getRow() - 2, Color.UNKNOWN)));
        }

        if ( this.getLocation (new GridCell (
                ficha.getColumn(), ficha.getRow() -2, Color.UNKNOWN)) != null) {
             ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn(), ficha.getRow() - 2, Color.UNKNOWN)));
        }

        if ( this.getLocation (new GridCell (
                ficha.getColumn() + 2, ficha.getRow() - 2, Color.UNKNOWN)) != null) {
            ret.add((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() + 2, ficha.getRow() - 2, Color.UNKNOWN)));
        }

        if ( this.getLocation (new GridCell (
                ficha.getColumn() - 2, ficha.getRow(), Color.UNKNOWN)) != null) {
            ret.add((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() - 2, ficha.getRow(), Color.UNKNOWN)));
        }

        if ( this.getLocation (new GridCell (
                ficha.getColumn() + 2, ficha.getRow(), Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() + 2, ficha.getRow(), Color.UNKNOWN)));
        }

        if ( this.getLocation (new GridCell (
                ficha.getColumn() - 2, ficha.getRow() + 2, Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() - 2, ficha.getRow() + 2, Color.UNKNOWN)));
        }

        if ( this.getLocation (new GridCell (
                ficha.getColumn(), ficha.getRow() + 2, Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn(), ficha.getRow() + 2, Color.UNKNOWN)));
        }

        if ( this.getLocation (new GridCell (
                ficha.getColumn() + 2, ficha.getRow() + 2, Color.UNKNOWN)) != null) {
            ret.add((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() + 2, ficha.getRow() + 2, Color.UNKNOWN)));
        }
        
        return ret;

    }

    @Transient
    public SortedSet<TablonFicha> getCross (TablonFicha ficha) {
        SortedSet<TablonFicha> ret = new TreeSet<TablonFicha>(new CellComparator());

        /* y, x -2 */
        if ( this.getLocation (new GridCell (
                ficha.getColumn(), ficha.getRow() -2, Color.UNKNOWN)) != null) {
             ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn(), ficha.getRow() - 2, Color.UNKNOWN)));
        }

        /* y, x + 2 */
        if ( this.getLocation (new GridCell (
                ficha.getColumn(), ficha.getRow() + 2, Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn(), ficha.getRow() + 2, Color.UNKNOWN)));
        }

        /* y + 2, x */
        if ( this.getLocation (new GridCell (
                ficha.getColumn() + 2, ficha.getRow(), Color.UNKNOWN)) != null) {
            ret.add ((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() + 2, ficha.getRow(), Color.UNKNOWN)));
        }

        /* y - 2, x */
        if ( this.getLocation (new GridCell (
                ficha.getColumn() - 2, ficha.getRow(), Color.UNKNOWN)) != null) {
            ret.add((TablonFicha) this.getLocation (new GridCell (
                ficha.getColumn() - 2, ficha.getRow(), Color.UNKNOWN)));
        }

        return ret;
    }

    @Transient
    public SortedSet<TablonFicha> getWaterTokens () {
        SortedSet<TablonFicha> ret = new TreeSet<TablonFicha>(new CellComparator());
        for (GridCell cell : this.getCells()) {
            TablonFicha ficha = (TablonFicha) cell;
            if (ficha.getType().equals(TokenType.WATER_PARTICLE))
                ret.add(ficha);
            else
                continue;
        }

        return ret;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        GameGrid grid = (GameGrid) super.clone();
        TablonGrid ret = new TablonGrid();
        for (GridCell cell : grid.getCells()) {
            ret.updateCell((TablonFicha) cell);
        }
        return ret;        
    }
}
