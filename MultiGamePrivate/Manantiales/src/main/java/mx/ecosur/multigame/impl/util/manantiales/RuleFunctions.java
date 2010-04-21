/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

package mx.ecosur.multigame.impl.util.manantiales;

import mx.ecosur.multigame.impl.Color;

import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridMove;
import mx.ecosur.multigame.impl.model.GridPlayer;

import mx.ecosur.multigame.impl.entity.manantiales.Ficha;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesPlayer;

import mx.ecosur.multigame.impl.enums.manantiales.BorderType;
import mx.ecosur.multigame.impl.enums.manantiales.ConditionType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;

import java.util.HashSet;
import java.util.List;

/**
 * @author awaterma@ecosur.mx
 */
public class RuleFunctions {

    public static  boolean isValidReplacement (GameGrid grid, ManantialesMove move) {
        boolean ret = false;
        if (move.getDestinationCell() != null) {
            Ficha destination = (Ficha) move.getDestinationCell();
            Ficha current = (Ficha) grid.getLocation (destination);
            if (current != null) {
               switch (current.getType()) {
                   case MANAGED_FOREST:
                       ret = !destination.getType().equals(TokenType.SILVOPASTORAL) &&
                           !destination.getType().equals(TokenType.INTENSIVE_PASTURE);
                       break;
                   case MODERATE_PASTURE:
                       ret = !destination.getType().equals(TokenType.SILVOPASTORAL);
                       break;
                   case VIVERO:
                       ret = !destination.getType().equals(TokenType.INTENSIVE_PASTURE);
                       break;
                   case SILVOPASTORAL:
                       ret = true;
                        break;
                   default:
                       ret = false;
                       break;
               }
            } else {
               ret = true;
            }
        }

        return ret;
    }

    public static  int score (ManantialesPlayer player, ManantialesMove move) {
        if (move.getCurrentCell() != null) {
            // decrement count of current
            switch (move.getReplacementType()) {
            case MANAGED_FOREST:
                player.setForested(player.getForested() - 1);
                break;
            case MODERATE_PASTURE:
                player.setModerate(player.getModerate() - 1);
                break;
            case INTENSIVE_PASTURE:
                player.setIntensive(player.getIntensive() - 1);
                break;
            case VIVERO:
                player.setVivero(player.getVivero() - 1);
                break;
            case SILVOPASTORAL:
                player.setSilvo(player.getSilvo() - 1);
                break;
            default:
                break;
            }
        }
        
        if (move.getDestinationCell() != null) {
	        switch (move.getType()) {
	            case MANAGED_FOREST:
	                player.setForested(player.getForested() +  1);
	                break;
	            case MODERATE_PASTURE:
	                player.setModerate(player.getModerate() + 1);
	                break;
	            case INTENSIVE_PASTURE:
	                player.setIntensive(player.getIntensive() + 1);
	                break;
	            case VIVERO:
	                player.setVivero(player.getVivero() + 1);
	                break;
	            case SILVOPASTORAL:
	                player.setSilvo(player.getSilvo() + 1);
	                break;
	            default:
	                break;
	        }
        }

        int forested = player.getForested() * 1;
        int moderate = player.getModerate() * 2;
        int intensive = player.getIntensive() * 3;
        int silvo = player.getSilvo() * 4;

        return forested + moderate + intensive + silvo;
    }

    public static  boolean isWinner (Mode mode, ManantialesPlayer gamePlayer) {
        boolean ret = false;
        ManantialesPlayer player = (ManantialesPlayer) gamePlayer;
        if (mode != Mode.BASIC_PUZZLE && mode != Mode.SILVO_PUZZLE)
            ret =  player.getScore() >= mode.getWinningScore();
        return ret;
    }

    public static boolean isSolved (Mode mode, ManantialesGame game) {
        boolean ret = true;
        for (GridPlayer gridPlayer : game.getPlayers()) {
            ManantialesPlayer player = (ManantialesPlayer) gridPlayer;
            ret = ret && ( player.getScore() >= mode.getWinningScore() );
        }

        return ret;
    }

    public static  boolean isBorder (GridCell cell) {
        return (cell.getColumn () == 4 || cell.getRow () == 4);
    }

    public static  GridPlayer incrementTurn (ManantialesGame game, GridMove move) {
        GridPlayer player = move.getPlayer();
        player.setTurn(false);
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

    public static  boolean isPrecedingPlayer (ManantialesGame game, GridPlayer first, GridPlayer second) {
        boolean ret = false;
        if (!first.equals(second)) {
            List<GridPlayer> players = game.getPlayers();
            int playerNumber = players.indexOf(first);
            GridPlayer nextPlayer = null;
            if (playerNumber == players.size() - 1) {
                nextPlayer = players.get(0);
            } else {
                nextPlayer = players.get(playerNumber + 1);
            }
            ret = nextPlayer.equals(second);
        }
        return ret;
    }


    public static  GameGrid clearPlayer (ManantialesGame game, ManantialesPlayer player) {
        GameGrid grid = game.getGrid();

        for (GridCell cell : game.getGrid().getCells()) {
            if (cell.getColor().equals(player.getColor()))
                grid.removeCell(cell);
        }

        return grid;
    }

    public static  GameGrid clearBorder (ManantialesGame game, ConditionType violation) {
        GameGrid ret = game.getGrid();
        HashSet<Ficha> deletions = new HashSet<Ficha>();

        switch (violation) {
            case NORTHERN_BORDER_DEFORESTED:
                for (GridCell cell : ret.getCells()) {
                    Ficha ficha = (Ficha) cell;
                    if (ficha.getBorder().equals(BorderType.NORTH))
                        deletions.add(ficha);
                }
                break;
            case WESTERN_BORDER_DEFORESTED:
                for (GridCell cell : ret.getCells()) {
                    Ficha ficha = (Ficha) cell;
                    if (ficha.getBorder().equals(BorderType.WEST))
                        deletions.add(ficha);
                }
                break;
            case SOUTHERN_BORDER_DEFORESTED:
                for (GridCell cell : ret.getCells()) {
                    Ficha ficha = (Ficha) cell;
                    if (ficha.getBorder().equals(BorderType.SOUTH))
                        deletions.add(ficha);
                }
                break;
            case EASTERN_BORDER_DEFORESTED:
                for (GridCell cell : ret.getCells()) {
                    Ficha ficha = (Ficha) cell;
                    if (ficha.getBorder().equals(BorderType.EAST))
                        deletions.add(ficha);
                }
                break;
            default:
                break;
        }

        for (Ficha ficha : deletions)
            ret.removeCell(ficha);

        return ret;
    }

    public static  GameGrid clearTerritory (ManantialesGame game, Color color) {
        GameGrid ret = game.getGrid();
        HashSet<Ficha> deletions = new HashSet<Ficha>();
        for (GridCell cell : ret.getCells()) {
            if (cell.getColor().equals(color)) {
                Ficha ficha = (Ficha) cell;
                if (ficha.getType().equals(TokenType.MANAGED_FOREST))
                    continue;
                deletions.add(ficha);
            }
        }

        for (Ficha ficha : deletions) {
            ret.removeCell (ficha);
        }

        return ret;
    }

    public static  Mode incrementMode (Mode mode) {
        if (mode == null) {
            mode = Mode.CLASSIC;
        } else {
            switch (mode) {
                case CLASSIC:
                    mode = Mode.BASIC_PUZZLE;
                    break;
                case BASIC_PUZZLE:
                    mode = Mode.SILVOPASTORAL;
                    break;
                case SILVOPASTORAL:
                    mode = Mode.SILVO_PUZZLE;
                    break;
                default:
                    break;
            }
        }
        return mode;
    }

}
