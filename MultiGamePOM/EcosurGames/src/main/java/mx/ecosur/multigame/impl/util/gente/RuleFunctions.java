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
import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridMove;
import mx.ecosur.multigame.impl.model.gente.BeadString;
import mx.ecosur.multigame.impl.entity.oculto.*;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;
import mx.ecosur.multigame.impl.entity.gente.GentePlayer;
import mx.ecosur.multigame.impl.enums.oculto.*;
import mx.ecosur.multigame.impl.Color;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.awt.*;


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
    public static boolean hasTria (GridCell cell1, GridCell cell2, GridCell cell3, GenteMove move) {
        BeadString tria = new BeadString(cell1, cell2, cell3);
        for (BeadString test : move.getTrias()) {
            if (test.equals(tria))
                return true;
        }

        return false;
    }

    /* Determines if a Tessera has already been scored*/
    public static boolean hasTessera (GridCell cell1, GridCell cell2, GridCell cell3,
        GridCell cell4, GenteMove move)
    {
        BeadString tessera = new BeadString (cell1, cell2, cell3, cell4);
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
}
