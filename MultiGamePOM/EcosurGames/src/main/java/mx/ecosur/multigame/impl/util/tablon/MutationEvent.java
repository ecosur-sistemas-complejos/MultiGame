/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 *
 * RuleFunctions.  A class that holds all of the
 * previous inline functions (as expressed previously
 * in Manantiales) in static methods for import into
 * the Oculto drl.
 *
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl.util.tablon;

import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.CellComparator;
import mx.ecosur.multigame.model.implementation.ConditionImpl;

import java.util.SortedSet;
import java.util.TreeSet;

public class MutationEvent implements ConditionImpl {

    SortedSet<TablonFicha> square, octogon, cross;
    TablonFicha ficha;

    public MutationEvent() {
        square = new TreeSet<TablonFicha>(new CellComparator());
        octogon = new TreeSet<TablonFicha>(new CellComparator());
        cross = new TreeSet<TablonFicha>(new CellComparator());
    }

    public MutationEvent(TablonFicha ficha) {
        this ();
        this.ficha = ficha;
    }

    public MutationEvent(TablonFicha ficha, SortedSet<TablonFicha> square, SortedSet<TablonFicha> octogon, SortedSet<TablonFicha> cross) {
        this.ficha = ficha;
        this.square = square;
        this.octogon = octogon;
        this.cross = cross;
    }

    public TablonFicha getFicha() {
        return ficha;
    }

    public void setFicha(TablonFicha ficha) {
        this.ficha = ficha;
    }

    public SortedSet<TablonFicha> getCross() {
        return cross;
    }

    public void setCross(SortedSet<TablonFicha> cross) {
        this.cross = cross;
    }

    public SortedSet<TablonFicha> getSquare() {
        return square;
    }

    public void setSquare(SortedSet<TablonFicha> square) {
        this.square = square;
    }

    public SortedSet<TablonFicha> getOctogon() {
        return octogon;
    }

    public void setOctogon(SortedSet<TablonFicha> octogon) {
        this.octogon = octogon;
    }

    public String getReason() {
        return "Mutation of " + ficha;
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
