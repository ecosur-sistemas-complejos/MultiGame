/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * The GameGrid class holds the current state of a specific game.  This class
 * is intended to be a transitive object, in the sense that a specific 
 * SharedBoardwill return a populated GameGrid to a specific caller. This 
 * hides gamegrid specific information for runtime callers; allowing quick
 * access to specific cells within a running game.
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame.grid.model;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import org.hibernate.annotations.Sort;

@Entity
public class GameGrid implements Serializable, Cloneable {

    private static final long serialVersionUID = -2579204312184918693L;

    Set<GridCell> cells;

    private int id;

    public GameGrid () {
        cells = new TreeSet<GridCell>(new CellComparator());
    }

    /**
     * Initializes a GameGrid object containing the List of Cells.
     */
    public GameGrid(TreeSet<GridCell> cells) {
        this.cells = cells;
    }

    @Id
    @GeneratedValue
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GridCell getLocation (GridCell location) {
        GridCell ret = null;
        if (location != null) {
            CellComparator comparator = new CellComparator();
            TreeSet<GridCell> treeSet = new TreeSet<GridCell>(comparator);
            for (GridCell cell : cells) {
                treeSet.add(cell);
            }

            SortedSet<GridCell> sublist = treeSet.tailSet(location);

            for (GridCell c : sublist) {
                int value = comparator.compare(location, c);
                if (value == 0) {
                    ret = c;
                    break;
                }
            }
        }

        return ret;
    }

    public void updateCell (GridCell cell) {
        if (cells == null)
            cells = new TreeSet<GridCell>(new CellComparator());
        if (cells.contains(cell))
                cells.remove(cell);
        cells.add(cell);
    }

    public void removeCell (GridCell cell) {
        cells.remove(cell);
    }

    @OneToMany (cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
    @Sort(comparator=CellComparator.class)
    public Set<GridCell> getCells () {
        if (cells == null)
            cells = new TreeSet<GridCell>(new CellComparator());
        return cells;
    }

    public void setCells(Set<GridCell> c){
        this.cells = new TreeSet<GridCell> (new CellComparator());
        this.cells.addAll(cells);
    }

    public String toString () {
        StringBuffer buf = new StringBuffer();
        Iterator<GridCell> iter = cells.iterator();
        buf.append ("GameGrid: [");
        while (iter.hasNext()) {
            GridCell cell = iter.next();
            buf.append("column: " + cell.getColumn() + ", row: " +
                            cell.getRow() + ", color: " + cell.getColor());
            if (iter.hasNext()) {
                    buf.append ("; ");
            }
            else {
                    buf.append ("]");
            }
        }

        return buf.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        GameGrid ret = new GameGrid();
        for (GridCell cell : cells) {
                GridCell cloneCell = cell.clone();
                ret.updateCell(cloneCell);
        }

        return ret;
    }
}
