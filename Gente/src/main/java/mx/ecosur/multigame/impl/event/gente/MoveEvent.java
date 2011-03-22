/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl.event.gente;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.enums.Direction;
import mx.ecosur.multigame.grid.util.BeadString;
import mx.ecosur.multigame.grid.model.GridCell;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.SortedSet;
import java.util.TreeSet;

public class MoveEvent {

    public GridCell origin;

    public SortedSet<GridCell> adjacentCells;

    public Direction direction;

    public MoveEvent () {
        origin = null;
        adjacentCells = new TreeSet<GridCell>(new CellComparator());
        direction = Direction.UNKNOWN;
    }

    public MoveEvent (GridCell origin, SortedSet<GridCell> adjacentCells) {
        this.origin = origin;
        this.adjacentCells = adjacentCells;
        this.direction = Direction.UNKNOWN;
    }

    public MoveEvent (Direction direction, GridCell origin, SortedSet<GridCell> adjacentCells) {
        this.direction = direction;
        this.origin = origin;
        this.adjacentCells = adjacentCells;
    }

    public GridCell getOrigin() {
        return origin;
    }

    public void setOrigin(GridCell origin) {
        this.origin = origin;
    }

    public SortedSet<GridCell> getAdjacentCells() {
        return adjacentCells;
    }

    public void setAdjacentCells(SortedSet<GridCell> adjacentCells) {
        this.adjacentCells = adjacentCells;
    }

    public Direction getDirection() {
        if (direction == Direction.UNKNOWN)
           direction = determineDirection();
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getSize () {
        int size = 0;
        if (origin != null)
            size++;
        if (adjacentCells != null)
            size += adjacentCells.size();
        return size;
    }

    public BeadString toBeadString () {
        BeadString ret = new BeadString();
        ret.add(origin);
        if (adjacentCells != null) {
            for (GridCell cell : adjacentCells) {
                ret.add(cell);
            }
        }

        return ret;
    }

    private Direction determineDirection() {
        Direction ret = Direction.UNKNOWN;
        if (origin == null || adjacentCells == null)
            return Direction.UNKNOWN;
        if (adjacentCells != null) {
            GridCell first = adjacentCells.first();
            if (first.getRow() == origin.getRow()) {
                if (first.getColumn() == origin.getColumn() - 1)
                    ret = Direction.NORTH;
                else if (first.getColumn() == origin.getColumn() + 1)
                    ret = Direction.SOUTH;
            } else if (first.getColumn () == origin.getColumn()) {
                    if (first.getRow() == origin.getRow () - 1)
                        ret = Direction.WEST;
                    else if (first.getRow() == origin.getRow () + 1)
                        ret = Direction.EAST;
            } else {
                if (first.getRow() == origin.getRow () + 1 && first.getColumn() == origin.getColumn() + 1)
                    ret = Direction.NORTHEAST;
                else if (first.getRow() == origin.getRow() + 1 && first.getColumn() == origin.getColumn() - 1)
                    ret = Direction.SOUTHEAST;
                else if (first.getRow() == origin.getRow () - 1 && first.getColumn() == origin.getColumn () - 1)
                    ret = Direction.NORTHWEST;
                else if (first.getRow() == origin.getRow() - 1 && first.getColumn() == origin.getColumn() + 1)
                    ret = Direction.SOUTHWEST;
            }
        }

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;

        if (obj instanceof MoveEvent) {
            MoveEvent comparison = (MoveEvent) obj;
            if (comparison.getSize() == this.getSize()) {
                ret = comparison.getOrigin().equals(this.origin) &&
                    comparison.getAdjacentCells().equals(this.getAdjacentCells());
            }
        }

        return ret;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(origin).append(adjacentCells).toHashCode();
    }
}
