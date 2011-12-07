package mx.ecosur.multigame.gente.util;

import mx.ecosur.multigame.gente.entity.GenteGame;
import mx.ecosur.multigame.gente.entity.GenteMove;
import mx.ecosur.multigame.gente.entity.GentePlayer;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.enums.Direction;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.util.Search;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 4/8/11
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentRuleFunctions {

    public static List<GridCell> findUnboundAdjacentCells (GenteGame game, Set<Color> colors) {
        List<GridCell> ret = new ArrayList<GridCell>();
        TreeSet<GridCell> candidates = new TreeSet<GridCell> (new CellComparator());
        Search search = new Search(game.getGrid());

        /* Get all Cells of with the targeted Colors */
        for (GridCell cell : game.getGrid().getCells ()) {
            if (colors.contains(cell.getColor())) {
                candidates.add(cell);
            }
        }

        /* Do a search with a depth of 1 for all of those cells, if a null
         * is returned, create a new cell and add it to the unbound list */
        for (GridCell candidate : candidates) {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.UNKNOWN)
                    continue;
                GridCell result = search.searchGrid(direction, candidate, 1);
                if (result == null) {
                    ret.add(createDestination (direction, candidate, 1));
                }
            }
        }

        return ret;
    }

    public static List<GenteMove> determineAvailableMoves (GenteGame game, GentePlayer player, HashSet<Color> colors) {
        ArrayList<GenteMove> ret = new ArrayList<GenteMove>();
        List<GridCell> unbound = findUnboundAdjacentCells(game, colors);
        for (GridCell cell : unbound){
                for (Color color : colors) {
                        cell.setColor(color);
                        ret.add(new GenteMove (player, cell));
                }
        }

        return ret;
    }

    public static List<GenteMove> determineAvailableMoves (GenteGame game, GentePlayer player, Color color) {
        HashSet<Color> colors = new HashSet<Color>();
        colors.add(color);
        return determineAvailableMoves (game, player, colors);
    }

    public static Set<Color> oppositionColors(GentePlayer player) {
        Set <Color> ret = new HashSet<Color> ();
        HashSet<Color> teamColors = new HashSet<Color>();
        teamColors.add(player.getColor());
        teamColors.add (player.getColor().getCompliment());
        for (Color color : Color.values()) {
        if (!teamColors.contains(color))
            ret.add(color);
        }
        return ret;
    }

    public static int getMaxId (GenteGame game) {
        int max = 0;
        Set<GridCell> cells = game.getGrid().getCells();
        for (GridCell cell : cells) {
            if (cell.getId() > max)
                max = cell.getId();
        }

        return max;
    }

    public static GridCell createDestination(Direction direction, GridCell cell, int factor) {
        int column = 0, row = 0;
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

        return new GridCell (column, row, Color.UNKNOWN);
    }
}
