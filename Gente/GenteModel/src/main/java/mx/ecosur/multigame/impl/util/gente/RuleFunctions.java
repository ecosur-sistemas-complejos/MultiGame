/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
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
import mx.ecosur.multigame.grid.enums.Vertice;
import mx.ecosur.multigame.grid.util.BeadString;
import mx.ecosur.multigame.grid.entity.GameGrid;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.impl.entity.gente.*;
import mx.ecosur.multigame.impl.event.gente.MoveEvent;

import java.util.*;
import java.awt.Dimension;
import java.util.List;


public class RuleFunctions {

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
        if (player.getTrias() != null && player.getTrias().size() >= 2)
            ret = 10;
        else if (player.getTesseras() != null && player.getTesseras().size() >= 3)
            ret = 5;
        return ret;
    }

    public static GridPlayer incrementTurn (GenteGame game, GenteMove move) {
        GridPlayer player = move.getPlayer();
        player.setTurn(false);

        /* Find next player */
        Set<GridPlayer> players = game.getPlayers();
        GridPlayer [] gps = players.toArray(new GridPlayer[players.size()]);
        int playerNumber = -1;

        for (int i = 0; i < gps.length; i++) {
            if (gps [ i ].equals(player)) {
                playerNumber = i;
                break;
            }
        }

        if (playerNumber == -1)
            throw new RuntimeException ("Unable to find player: " + player + " in set " + Arrays.toString(gps));

        GridPlayer nextPlayer = null;
        if (playerNumber == gps.length - 1) {
            nextPlayer = gps [ 0 ];
        } else {
            nextPlayer = gps [playerNumber + 1];
        }

        nextPlayer.setTurn (true);
        return nextPlayer;
    }

    public static boolean hasTria (GentePlayer player, BeadString test) {
        for (Tria t : player.getTrias()) {
            BeadString string = new BeadString();
            for (GridCell c : t.getCells()) {
                string.add(c);
            }
            if (string.equals(test) || string.contains(test))
                return true;
        }
        
        return false;
    }

    public static boolean hasTessera (GentePlayer player, BeadString test) {
        for (Tessera t : player.getTesseras()) {
            BeadString string = new BeadString();
            for (GridCell c : t.getCells()) {
                string.add(c);
            }
            if (string.equals(test) || string.contains(test))
                return true;
        }

        /* Check Partner */
        GentePlayer partner = player.getPartner();
        for (Tessera t : partner.getTesseras()) {
            BeadString string = new BeadString();
            for (GridCell c : t.getCells()) {
                string.add(c);
            }
            if (test.contains(string))
                return true;
        }

        return false;
    }

    public static boolean hasMultipleColors (MoveEvent event) {
        HashSet<mx.ecosur.multigame.grid.Color> colors = new HashSet<mx.ecosur.multigame.grid.Color>();
        for (GridCell cell : event.getAdjacentCells()) {
            colors.add(cell.getColor());
        }
        colors.add(event.getOrigin().getColor());
        return (colors.size () > 1);
    }

    public static Set<MoveEvent> generateEvents (GameGrid grid, GenteMove move) {
        Set<MoveEvent> ret = new HashSet<MoveEvent>();
        mx.ecosur.multigame.grid.Color[] team = new mx.ecosur.multigame.grid.Color[ 2 ];
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
                                                  mx.ecosur.multigame.grid.Color... colors)
    {
        Set<MoveEvent> ret = new HashSet<MoveEvent> ();
        GridCell origin = move.getDestinationCell();
        List<mx.ecosur.multigame.grid.Color> teamColors = Arrays.asList(colors);

        /* Returns both tria (one color, three slots) and tessera (two colors, four slots)
           permutations in the specified plane from the specified GameGrid using the move's
           destination cell as the origin.
            */
        switch (vertice) {
            case VERTICAL:
                BeadString beadPlane = new BeadString ();
                for (int i = -1 * (length); i < length; i++) {
                    GridCell location = grid.getLocation(new GridCell(origin.getColumn(), origin.getRow() + i,
                            mx.ecosur.multigame.grid.Color.UNKNOWN));
                    if (location != null && teamColors.contains(location.getColor()))
                        beadPlane.add(location);
                }
                ret.addAll (getPermutations (origin, beadPlane, length));
                break;
            case HORIZONTAL:
                beadPlane = new BeadString ();
                for (int i = -1 * (length); i < length; i++) {
                    GridCell location = grid.getLocation(new GridCell (origin.getColumn() + i, origin.getRow(),
                            mx.ecosur.multigame.grid.Color.UNKNOWN));
                    if (location != null && teamColors.contains(location.getColor()))
                        beadPlane.add(location);
                }
                ret.addAll (getPermutations (origin, beadPlane, length));
                break;
            case FORWARD:
                beadPlane = new BeadString ();
                for (int i = -1 * (length); i < length; i++) {
                    GridCell location = grid.getLocation(new GridCell (origin.getColumn() + i, origin.getRow() - i,
                            mx.ecosur.multigame.grid.Color.UNKNOWN));
                    if (location != null && teamColors.contains(location.getColor()))
                        beadPlane.add(location);
                }
                ret.addAll (getPermutations (origin, beadPlane, length));
                break;
            case REVERSE:
                beadPlane = new BeadString ();
                for (int i = -1 * (length); i < length; i++) {
                    GridCell location = grid.getLocation(new GridCell (origin.getColumn() + i, origin.getRow() + i,
                            mx.ecosur.multigame.grid.Color.UNKNOWN));
                    if (location != null && teamColors.contains(location.getColor()))
                        beadPlane.add(location);
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
            BeadString plane = new BeadString();
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
                    if (candidate.toBeadString().isContiguous(vertice) && candidate.getSize() == length) {
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
                    if (candidate.toBeadString().isContiguous(vertice) && candidate.getSize() == length) {
                        ret.add(candidate);
                        break;
                    }
                }
            }

            /* add all supposedly adjacent cells and origin */
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
                if (candidate.toBeadString().isContiguous(vertice) && candidate.getSize() == length) {
                    ret.add(candidate);
                    break;
                }
            }
        }        

        return ret;
    }
}
