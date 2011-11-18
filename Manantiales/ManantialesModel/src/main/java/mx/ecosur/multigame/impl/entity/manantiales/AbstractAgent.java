package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.GameGrid;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.model.interfaces.Agent;
import mx.ecosur.multigame.model.interfaces.Move;

import java.io.Serializable;
import java.util.*;

public abstract class AbstractAgent extends ManantialesPlayer implements Agent, Serializable {


    public AbstractAgent() {
        super();
    }

    public AbstractAgent(GridRegistrant player, Color favoriteColor) {
        super(player, favoriteColor);
    }

    protected Set<Move> findNewMoves(ManantialesGame game) {
        Set<Move> ret = new LinkedHashSet<Move>();

        List<ManantialesFicha> fichas = generateCandidates(game);
        for (ManantialesFicha ficha : fichas) {
            if (isGoodLocation(ficha)) {
                ManantialesMove move = new ManantialesMove();
                move.setPlayer(this);
                move.setDestinationCell(ficha);
                move.setMode(game.getMode());
                ret.add(move);
            }
        }

        return ret;
    }

    protected Set<Move> findUpgradeMoves(ManantialesGame game) {
        Set<Move> ret = new LinkedHashSet<Move>();

        Set<GridCell> filter = new HashSet<GridCell>();
        for (GridCell cell : game.getGrid().getCells()) {
            if (cell != null && getColor() != null && cell.getColor().equals(getColor())) {
                filter.add(cell);
            }
        }

        for (GridCell cell : filter) {
            ManantialesMove move = new ManantialesMove();
            move.setPlayer(null);
            move.setMode(game.getMode());

            ManantialesFicha ficha = (ManantialesFicha) cell;
            /* Convert Moderate to Intensive */
            if (ficha.getType().equals(TokenType.MODERATE_PASTURE) && getIntensive() < 6) {
                ManantialesFicha destination = new ManantialesFicha(ficha.getColumn(), ficha.getRow(), ficha.getColor(), ficha.getType());
                destination.setType(TokenType.INTENSIVE_PASTURE);
                move.setCurrentCell(ficha);
                move.setDestinationCell(destination);
            } else if (ficha.getType().equals(TokenType.MANAGED_FOREST) && getModerate() < 6) {
                ManantialesFicha destination = new ManantialesFicha(ficha.getColumn(), ficha.getRow(), ficha.getColor(), ficha.getType());
                destination.setType(TokenType.MODERATE_PASTURE);
                move.setCurrentCell(ficha);
                move.setDestinationCell(destination);
            }
            if (move.getDestinationCell() != null) {
                move.setPlayer(null);
                ret.add(move);
            }
        }

        return ret;
    }

    protected Set<Move> findSwapMoves(ManantialesGame game) {
        Set<Move> ret = Collections.EMPTY_SET;
        Set<GridCell> filter = new HashSet<GridCell>();
        for (GridCell cell : game.getGrid().getCells()) {
            ManantialesFicha ficha = (ManantialesFicha) cell;
            if (getColor() != null && ficha.getColor().equals(getColor()) &&
                    ficha.getType().equals(TokenType.MANAGED_FOREST)) {
                filter.add(cell);
            }
        }

        /* Remove two fichas from the list, create a new Swap move based on those fichas and continue */


        return ret;
    }

    private List<ManantialesFicha> generateCandidates(ManantialesGame game) {
        List<ManantialesFicha> ret = new ArrayList<ManantialesFicha>();
        GameGrid grid = game.getGrid();

        int startrow, startcol, endrow, endcol;
        switch (getColor()) {
            case YELLOW:
                startrow = 0;
                endrow = 4;
                startcol = 0;
                endcol = 4;
                break;
            case PURPLE:
                startrow = 4;
                endrow = game.getRows();
                startcol = 0;
                endcol = 4;
                break;
            case RED:
                startrow = 0;
                endrow = 4;
                startcol = 4;
                endcol = game.getColumns();
                break;
            case BLACK:
                startrow = 4;
                endrow = game.getRows();
                startcol = 4;
                endcol = game.getColumns();
                break;
            default:
                throw new RuntimeException("Unknown Color!!");
        }

        for (int row = startrow; row <= endrow; row++) {
            for (int col = startcol; col <= endcol; col++) {
                ManantialesFicha ficha = new ManantialesFicha(col, row, getColor(), TokenType.UNKNOWN);
                if (isGoodLocation(ficha) && grid.getLocation(ficha) == null) {
                    if (getForested() < 6)
                        ficha.setType(TokenType.MANAGED_FOREST);
                    else if (getModerate() < 6)
                        ficha.setType(TokenType.MODERATE_PASTURE);
                    else if (game.getMode().equals(Mode.SILVOPASTORAL) && getVivero() < 6)
                        ficha.setType(TokenType.VIVERO);
                    ret.add(ficha);
                }
            }
        }

        return ret;
    }

    private boolean isGoodLocation(ManantialesFicha ficha) {
        boolean ret;
        Color color = ficha.getColor();
        int column = ficha.getColumn(), row = ficha.getRow();

        switch (color) {
            case YELLOW:
                ret = (column < 5 && row < 5);
                break;
            case PURPLE:
                ret = (column < 5 && row > 3);
                break;
            case RED:
                ret = (column > 3 && row < 5);
                break;
            case BLACK:
                ret = (column > 3 && row > 3);
                break;
            default:
                ret = false;
        }

        if (column == 4 || row == 4) {

            /* Check for Manantial */
            if (column == 4 && row == 4)
                ret = false;
        } else {
            if (row % 2 == 0 && column % 2 == 0) {
                // even
                ret = ret && true;

            } else if (row % 2 != 0 && column % 2 != 0) {
                //odd
                ret = ret && true;
            } else
                ret = false;
        }

        if (row > 8 || column > 8)
            ret = false;

        return ret;
    }
}
