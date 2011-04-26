/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * Search utilities for anlayzing grids.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.grid.util;

import java.io.Serializable;
import java.util.*;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.enums.Direction;
import mx.ecosur.multigame.grid.entity.GameGrid;
import mx.ecosur.multigame.grid.entity.GridCell;

import mx.ecosur.multigame.grid.enums.Vertice;

public class Search implements Serializable {
        
        private static final long serialVersionUID = -4388187588225945478L;
        
        private GameGrid grid;

        public Search (GameGrid grid) {
                this.grid = grid;
        }
        
        /*
         * Returns a list of annotated cells, from a starting point.  If the 
         * annotated starting point has a known direction, then search will
         * be performed in that direction.  If the direction is "UNKNOWN", 
         * a search will be performed in all 8 directions to the specified depth.
         *
         */
        public Set<AnnotatedCell> findCandidates(AnnotatedCell startingCell, 
                        GridCell destination, Color[] targetColors, int depth)
        {
            Set<AnnotatedCell> ret = new HashSet<AnnotatedCell> ();

            /* If the annotated cell has no direction, search in all
             * adjacent directions to the requested depth, otherwise
             * search the grid in the named direction to the requested
             * depth.
             */
            if (startingCell.getDirection() == Direction.UNKNOWN) {
                Set<AnnotatedCell> adjacent = findAdjacentCells(targetColors, destination);
                /* Add the adjacent cells to the candidate list */
                ret.addAll(adjacent);

                for (AnnotatedCell direction : adjacent) {
                        ret.addAll(findCandidates (direction, destination, targetColors, depth));
                }
            } else {
                for (int i = 0; i < depth - 1; i++) {
                    for (Color targetColor : targetColors) {
                        GridCell result = this.searchGrid(startingCell.getDirection(),
                            startingCell.getCell(), targetColor, i + 1);
                        if (result != null && result.getColor() == targetColor) {
                            ret.add(new AnnotatedCell (result,
                              startingCell.getDirection()));
                            break;
                        }
                    }
                }
            }

            return ret;
        }
        
        /*
         * Check each cell next to the destination, if
         * a cell is found in that direction, add that 
         * cell to the list.
         */
        public Set<AnnotatedCell> findAdjacentCells(Color[] targetColors, GridCell destination) {
            HashMap <AnnotatedCell,Direction> adjacent = new HashMap <AnnotatedCell,Direction> ();
            for (Direction d : Direction.values()) {
                    for (Color color : targetColors) {
                        GridCell result = this.searchGrid (d, destination, color, 1);
                        if (result != null && !adjacent.containsValue(d) && result.getColor().equals(color))
                           adjacent.put (new AnnotatedCell (result,d),d);
                    }
                }
                
            return adjacent.keySet();
        }
        
        public GridCell searchGrid (Direction direction, GridCell cell, int factor) {
                return searchGrid (direction, cell, Color.UNKNOWN, factor);
        }

        /*
         * Returns a GridCell to be searched for within a grid, with a factor of 
         * change defined by "factor".  North is considered the "top" of the 
         * board, meaning the lowest row.  All other cardinal points are derived 
         * from this simple map.
         * @param direction
         * @param cell
         * @param factor
         * @return
         */
        public GridCell searchGrid(Direction direction, GridCell cell, Color color, int factor) {
            int row = 0, column = 0;
            switch (direction) {
                case NORTH:
                    column = cell.getColumn();
                    row = cell.getRow() - factor;
                    break;
                case SOUTH:
                    column = cell.getColumn();
                    row = cell.getRow() + factor;
                    break;
                case EAST:
                    column = cell.getColumn() + factor;
                    row = cell.getRow();
                    break;
                case WEST:
                    column = cell.getColumn () - factor;
                    row = cell.getRow();
                    break;
                case NORTHEAST:
                    column = cell.getColumn() + factor;
                    row = cell.getRow() - factor;
                    break;
                case NORTHWEST:
                    column = cell.getColumn () - factor;
                    row = cell.getRow() - factor;
                    break;
                case SOUTHEAST:
                    column = cell.getColumn() + factor;
                    row = cell.getRow () + factor;
                    break;
                case SOUTHWEST:
                    column = cell.getColumn () - factor;
                    row = cell.getRow() + factor;
                    break;
                default:
                    break;
            }

            GridCell searchCell = new GridCell (column, row, color);
            return grid.getLocation(searchCell);
        }
        
        public HashMap<Vertice, HashSet<BeadString>> getString (GridCell startingCell, int stringlength) {
                return getString (startingCell, stringlength, false);
        }
        
        public HashMap<Vertice, HashSet<BeadString>> getString(GridCell startingCell, int stringlength,
           boolean compliment)
        {
            HashMap<Vertice, HashSet<BeadString>> ret = new HashMap<Vertice, HashSet<BeadString>> ();
            AnnotatedCell start = new AnnotatedCell (startingCell);

            /* We are only interested in cells of this color */
            Color[] colors;
            if (compliment) {
                colors = new Color [ 2 ];
                colors [ 0 ] = startingCell.getColor();
                colors [ 1 ] = colors [ 0 ].getCompliment();
            } else {
                colors = new Color [ 1 ];
                colors [ 0 ] = startingCell.getColor();
            }

            /* Perform a search to the depth of stringlength + 1, to pickup
             * any non-valid configurations */
            Set<AnnotatedCell> candidates = findCandidates(start, startingCell,
                colors, stringlength + 1);

            /* Vertice Hash of cells */
            HashMap <Vertice,BeadString> directionalMap = new HashMap<Vertice,BeadString>();

            /* Sort the candidates by Vertice */
            for (AnnotatedCell candidate : candidates) {
                if (directionalMap.containsKey(candidate.getDirection().getVertice())) {
                    BeadString string = directionalMap.get(candidate.getDirection().getVertice());
                    string.add(candidate.getCell());
                    directionalMap.put (candidate.getDirection().getVertice(), string);
                } else {
                    BeadString string = new BeadString ();
                    string.add(candidate.getCell());
                    directionalMap.put(candidate.getDirection().getVertice(), string);
                }
            }

            for (Map.Entry<Vertice,BeadString> entry  : directionalMap.entrySet()) {
                Vertice v = entry.getKey();
                BeadString string = entry.getValue();
                /* Ensure the destination cell is within the candidate string */
                if (!string.contains(startingCell))
                    string.add(startingCell);

                /* Process the two types of strings differently */
                if (string.isTerminator(startingCell)) {
                    ret = processTerminalString(startingCell, stringlength, ret, v, string);
                } else {
                    ret = processMiddleString (startingCell, stringlength, ret, v, string);
                }
            }

            return ret;
        }

        private HashMap<Vertice, HashSet<BeadString>> processTerminalString( GridCell startingCell,
            int stringlength, HashMap<Vertice, HashSet<BeadString>> ret, Vertice v, BeadString string)
        {
            string = string.trim (startingCell, stringlength);
            if (string.size() == stringlength) {
                addString (ret, v, string);
            }
                
            return ret;
        }
        
        private HashMap<Vertice, HashSet<BeadString>> processMiddleString (GridCell startingCell,
            int stringlength, HashMap<Vertice, HashSet<BeadString>> ret, Vertice v, BeadString string)
        {
            /* Simple middle case */
            if (string.size() == stringlength) {
                    addString (ret, v, string);
            } else {
                /* Joined Case */
                SortedSet<GridCell> cells = string.getBeads();
                BeadString top = new BeadString ();
                BeadString bottom = new BeadString ();
                for (GridCell cell : cells) {
                    if (cell.equals(startingCell))
                        break;
                    top.add(cell);
                }

                bottom.setBeads(new TreeSet<GridCell>(cells.tailSet(startingCell)));

                /* ensure top and bottom have the destination cell */
                top.add(startingCell);
                bottom.add(startingCell);

                /* Add the top string if it is the requested length */
                if (top.size() == stringlength) {
                    addString (ret, v, top);
                }

                /* Add the bottom string if it is the requested length */
                if (bottom.size() == stringlength) {
                    addString (ret, v, bottom);
                }
            }

            return ret;
        }
        
        private void addString (HashMap<Vertice,HashSet<BeadString>> map, Vertice v,
                        BeadString string) {
                HashSet <BeadString> set = map.get(v);
                if (set == null)
                        set = new HashSet<BeadString>();
                set.add (string);
                map.put (v, set);
        }
}
