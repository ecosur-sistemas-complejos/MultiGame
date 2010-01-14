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
 * in Gente) in static methods for import into
 * the Gente drl.
 *
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl.util.gente;

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.impl.enums.Vertice;
import mx.ecosur.multigame.impl.event.gente.MoveEvent;
import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.util.BeadString;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;
import mx.ecosur.multigame.impl.entity.gente.GentePlayer;
import mx.ecosur.multigame.impl.Color;

import java.util.*;
import java.awt.*;
import java.util.List;


public class RuleFunctions {

        /**   Determines if the destination of the requested move
         is in the center of the grid.*/
    public static boolean isCenter (GenteGame game, GenteMove move) {
        Dimension size = game.getSize();
        GridCell destination = (GridCell) move.getDestinationCell ();

        int centerWidth = (int) size.getWidth()/2;
        int centerHeight = (int) size.getHeight()/2;

        boolean ret = (destination.getRow () == centerHeight &&
            destination.getColumn() == centerWidth);
        if (!ret)
            move.setStatus (MoveStatus.INVALID);
        return ret;
    }

    /*    Determines if the destination of the requested move is
        empty on the grid.*/
    public static boolean isEmpty (GameGrid grid, GenteMove move) {
        GridCell destination = (GridCell) move.getDestinationCell();
        GridCell current = grid.getLocation (destination);
        boolean ret = (current == null);
        if (!ret)
            move.setStatus (MoveStatus.INVALID);
        return ret;
    }


    public static int scorePlayer (GenteMove move) {
        int ret = 0;
        GentePlayer player = (GentePlayer) move.getPlayer();
        if (player.getTrias().size() >= 2)
            ret = 10;
        else if (player.getTesseras().size() >= 3)
            ret = 5;
        return ret;
    }

    /*    Determines if the game has a winner.  */
    public static boolean hasWinner (GenteGame game) {
        return (game.getWinners().size() > 0);
    }

    /*    Gets the winner from a specified game.  For more complicated payoff
        schemas, this function should return a PriorityQueue of Players
        based upon positions on the board.*/
    public static Set getWinners (GenteGame game) {
        GenteGame pente = (GenteGame) game;
        return pente.getWinners();
    }

    public static GridPlayer incrementTurn (GenteGame game, GenteMove move) {
        GridPlayer player = move.getPlayer();
        player.setTurn(false);

        /* Find next player */
        List<GridPlayer> players = game.getPlayers();
        int playerNumber = players.indexOf(player);
        GridPlayer nextPlayer = null;
        if (playerNumber == players.size() - 1) {
            nextPlayer = players.get(0);
        } else {
            nextPlayer = players.get(playerNumber + 1);
        }

        nextPlayer.setTurn (true);
        return nextPlayer;
    }

    /* Determines if a Tria has already been scored*/
    public static boolean hasTria (BeadString tria, GenteMove move) {
        for (BeadString test : move.getTrias()) {
            if (test.equals(tria))
                return true;
        }

        return false;
    }

    /* Determines if a Tessera has already been scored*/
    public static boolean hasTessera (BeadString tessera, GenteMove move) {
        for (BeadString test : move.getTesseras()) {
            if (test.equals(tessera))
                return true;
        }

        return false;
    }

    /* Determines if a group of cells is multi-colored*/
    public static boolean hasMultipleColors (GridCell cell1, GridCell cell2, GridCell cell3, GridCell cell4) {
        HashSet<Color> colors = new HashSet<Color>();
        colors.add(cell1.getColor());
        colors.add(cell2.getColor());
        colors.add(cell3.getColor());
        colors.add(cell4.getColor());
        return (colors.size () > 1);
    }

    public static boolean hasMultipleColors (MoveEvent event) {
        HashSet<Color> colors = new HashSet<Color>();
        for (GridCell cell : event.getAdjacentCells()) {
            colors.add(cell.getColor());
        }
        colors.add(event.getOrigin().getColor());        
        return (colors.size () > 1);
    }

    public static Set<MoveEvent> GenerateEvents (GameGrid grid, GenteMove move) {
        Set<MoveEvent> ret = new HashSet<MoveEvent>();
        Color [] team = new Color [ 2 ];
        team [ 0 ] = move.getDestinationCell().getColor();
        team [ 1 ] = team [0].getCompliment();

        /* Tesseras */
        for (Vertice vertice : Vertice.values()) {
            if (vertice.equals(Vertice.UNKNOWN))
                continue;
            ret.addAll (getPermutations (vertice, grid, move, 4, team));
        }

        /* Trias */
        for (Vertice vertice : Vertice.values()) {
            if (vertice.equals(Vertice.UNKNOWN))
                continue;
            ret.addAll (getPermutations (vertice, grid, move, 3, team [ 0 ]));
        }
        
        return ret;
    }

    public static Set<MoveEvent> getPermutations (Vertice vertice, GameGrid grid, GenteMove move, int length,
                                                  Color... colors)
    {
        Set<MoveEvent> ret = new HashSet<MoveEvent> ();
        GridCell origin = move.getDestinationCell();
        List teamColors = Arrays.asList(colors);

        /* Returns both tria (one color, three slots) and tessera (two colors, four slots)
           permutations in the specified plane from the specified GameGrid using the move's
           destination cell as the origin.
            */
        switch (vertice) {
            case VERTICAL:
                BeadString beadPlane = new BeadString ();
                for (int i = -1 * (length); i <= length; i++) {
                    GridCell location = grid.getLocation(new GridCell (origin.getColumn() + i, origin.getRow(),
                            Color.UNKNOWN));
                    if (location != null && teamColors.contains(location.getColor()))
                        beadPlane.add(location);
                }
                ret.addAll (getPermutations (origin, beadPlane, length));
                break;
            case HORIZONTAL:
                beadPlane = new BeadString ();
                for (int i = -1 * (length); i <= length; i++) {
                    GridCell location = grid.getLocation(new GridCell (origin.getColumn(), origin.getRow() + i,
                            Color.UNKNOWN));
                    if (location != null && teamColors.contains(location.getColor()))
                        beadPlane.add(location);
                }
                ret.addAll (getPermutations (origin, beadPlane, length));
                break;
            case FORWARD:
                beadPlane = new BeadString ();
                for (int i = -1 * (length); i <= length; i++) {
                    for (int j = -1 * (length); j <= length; j++) {
                        GridCell location = grid.getLocation(new GridCell (origin.getColumn() + j, origin.getRow() - i,
                                Color.UNKNOWN));
                        if (location != null && teamColors.contains(location.getColor()))
                            beadPlane.add(location);
                    }
                }
                ret.addAll (getPermutations (origin, beadPlane, length));
                break;
            case REVERSE:
                beadPlane = new BeadString ();
                for (int i = -1 * (length); i <= length; i++) {
                    for (int j = -1 * (length); j <= length; j++) {
                        GridCell location = grid.getLocation(new GridCell (origin.getColumn() + j, origin.getRow() + i,
                                Color.UNKNOWN));
                        if (location != null && teamColors.contains(location.getColor()))
                            beadPlane.add(location);
                    }
                }
                ret.addAll (getPermutations (origin, beadPlane, length));
                break;
            default:
                break;
        }
        return ret;
    }

    public static Set<MoveEvent> getPermutations (GridCell origin, BeadString string, int length) {
        Set<MoveEvent> ret = new HashSet<MoveEvent>();
        Set<BeadString> sets = new HashSet<BeadString>();

        if (string.getBeads().size() > length) {
            int counter = 0;
            BeadString plane = new BeadString ();
            sets.add(plane);
            for (GridCell cell : string.getBeads()) {
                if (plane.getBeads().size() == length) {
                    GridCell last = plane.getBeads().last();
                    plane = new BeadString();
                    plane.add(last);
                    sets.add(plane);
                }

                plane.add(cell);
            }
        } else {
            sets.add(string);
        }

        for (BeadString plane : sets) {
            /* tailset from origin */
            SortedSet<GridCell> tail = plane.getBeads().tailSet(origin);
            tail.remove(origin);
            if (tail.size() > 0) {
                MoveEvent candidate = new MoveEvent (origin, tail);
                for (Vertice vertice : Vertice.values()) {
                    if (vertice.equals(Vertice.UNKNOWN))
                        continue;
                    if (candidate.toBeadString().contiguous(vertice) && candidate.getSize() == length) {
                        ret.add(candidate);
                        break;
                    }
                }
            }

            /* headset from origin */
            SortedSet<GridCell> head = plane.getBeads().headSet(origin);
            head.remove(origin);
            if (head.size() > 0) {
                MoveEvent candidate = new MoveEvent (origin, head);
                for (Vertice vertice : Vertice.values()) {
                    if (vertice.equals(Vertice.UNKNOWN))
                        continue;
                    if (candidate.toBeadString().contiguous(vertice) && candidate.getSize() == length) {
                        ret.add(candidate);
                        break;
                    }
                }
            }

            /* add all supposedly adjacent cells and origin */
            int counter = 0;
            MoveEvent candidate = new MoveEvent();
            candidate.setOrigin(origin);
            for (GridCell cell : plane.getBeads()) {
                if (cell.equals(origin))
                    continue;
                candidate.adjacentCells.add(cell);
            }

            for (Vertice vertice : Vertice.values()) {
                if (vertice.equals(Vertice.UNKNOWN))
                    continue;
                if (candidate.toBeadString().contiguous(vertice) && candidate.getSize() == length) {
                    ret.add(candidate);
                    break;
                }
            }
        }        

        return ret;
    }
}
