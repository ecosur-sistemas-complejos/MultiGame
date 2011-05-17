/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 *
 *
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl.util.pasale;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.impl.entity.pasale.PasaleFicha;
import mx.ecosur.multigame.model.interfaces.Condition;

import java.util.SortedSet;
import java.util.TreeSet;

public class MutationEvent implements Condition {

    SortedSet<PasaleFicha> square, octogon, cross;
    PasaleFicha ficha;
    String reason;

    public MutationEvent() {
        square = new TreeSet<PasaleFicha>(new CellComparator());
        octogon = new TreeSet<PasaleFicha>(new CellComparator());
        cross = new TreeSet<PasaleFicha>(new CellComparator());
    }

    public MutationEvent(PasaleFicha ficha) {
        this ();
        this.ficha = ficha;
    }

    public MutationEvent(PasaleFicha ficha, SortedSet<PasaleFicha> square, SortedSet<PasaleFicha> octogon, SortedSet<PasaleFicha> cross) {
        this.ficha = ficha;
        this.square = square;
        this.octogon = octogon;
        this.cross = cross;
    }

    public PasaleFicha getFicha() {
        return ficha;
    }

    public void setFicha(PasaleFicha ficha) {
        this.ficha = ficha;
    }

    public SortedSet<PasaleFicha> getCross() {
        return cross;
    }

    public void setCross(SortedSet<PasaleFicha> cross) {
        this.cross = cross;
    }

    public SortedSet<PasaleFicha> getSquare() {
        return square;
    }

    public void setSquare(SortedSet<PasaleFicha> square) {
        this.square = square;
    }

    public SortedSet<PasaleFicha> getOctogon() {
        return octogon;
    }

    public void setOctogon(SortedSet<PasaleFicha> octogon) {
        this.octogon = octogon;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Object[] getTriggers() {
        Object [] ret = new Object [ 1 ];
        ret [ 0 ] = ficha;
        return ret;
    }

    @Override
    public String toString() {
        return "[MutationEvent] " + this.getReason();
    }
}
