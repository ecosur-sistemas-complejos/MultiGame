/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

package mx.ecosur.multigame.manantiales.util;

import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.grid.Color;

import mx.ecosur.multigame.grid.comparator.PlayerComparator;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.entity.GameGrid;
import mx.ecosur.multigame.grid.entity.GridPlayer;

import mx.ecosur.multigame.manantiales.entity.CheckCondition;
import mx.ecosur.multigame.manantiales.entity.ManantialesFicha;
import mx.ecosur.multigame.manantiales.entity.ManantialesGame;
import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;

import mx.ecosur.multigame.manantiales.enums.BorderType;
import mx.ecosur.multigame.manantiales.enums.ConditionType;
import mx.ecosur.multigame.manantiales.enums.Mode;
import mx.ecosur.multigame.manantiales.enums.TokenType;
import mx.ecosur.multigame.model.interfaces.Cell;

import java.awt.Point;
import java.util.*;

/**
 * RuleFunctions specific to the Manantiales de La Sierra DRL.
 *
 * @author awaterma@ecosur.mx
 */
public class RuleFunctions {

    public static boolean isWithinTimeLimit(ManantialesGame game, ManantialesMove move) {
        long maxTime = (long) (45 * 60) * 1000; // 45 minutes in ms
        return game.calculateElapsedTime() < maxTime;
    }

    public static  boolean isValidReplacement (GameGrid grid, ManantialesMove move) {
        boolean ret = false;
        if (move.getDestinationCell() != null) {
            ManantialesFicha destination = (ManantialesFicha) move.getDestinationCell();
            ManantialesFicha current = (ManantialesFicha) grid.getLocation (destination);
            TokenType replacement = destination.getType();
            if (current != null) {
               switch (current.getType()) {
                   case MANAGED_FOREST:
                       ret = !(replacement.equals(TokenType.INTENSIVE_PASTURE) || replacement.equals(TokenType.SILVOPASTORAL));
                       break;
                   case MODERATE_PASTURE:
                       ret = (replacement.equals(TokenType.INTENSIVE_PASTURE) || replacement.equals(TokenType.MANAGED_FOREST))
                               && !(replacement.equals(TokenType.SILVOPASTORAL));
                       break;
                   case VIVERO:
                       ret = replacement.equals(TokenType.SILVOPASTORAL);
                       break;
                   case SILVOPASTORAL:
                       ret = !(replacement.equals(TokenType.INTENSIVE_PASTURE));
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

    public static int score (ManantialesPlayer player, ManantialesGame game) {
        // reset player count to 0
        player.reset();

        for (GridCell cell : game.getGrid().getCells()) {
            if (!cell.getColor().equals(player.getColor()))
                continue;            
            ManantialesFicha ficha = (ManantialesFicha) cell;
            switch (ficha.getType()) {
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
    
    public static boolean effectsCondition (CheckCondition condition, ManantialesMove move) {
        boolean ret = false;
        
        switch (condition.getType()) {
            case NORTHERN_BORDER_DEFORESTED:
                if (move.getBorder() != null) ret = move.getBorder().equals(BorderType.NORTH);
                break;
            case SOUTHERN_BORDER_DEFORESTED:
                if (move.getBorder() != null) ret = move.getBorder().equals(BorderType.SOUTH);
                break;
            case WESTERN_BORDER_DEFORESTED:
                if (move.getBorder() != null) ret = move.getBorder().equals(BorderType.WEST);
                break;
            case EASTERN_BORDER_DEFORESTED:
                if (move.getBorder() != null) ret = move.getBorder().equals(BorderType.EAST);
                break;
            case MANANTIALES_DRY:
                ret = move.isManantial();
            default:
                break;
        }
        
        return ret;
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
        
        ret = ret && (game.getCheckConditions().size() == 0);

        return ret;
    }

    public static  boolean isBorder (GridCell cell) {
        return (cell.getColumn () == 4 || cell.getRow () == 4);
    }

    public static GridPlayer incrementTurn (ManantialesGame game, ManantialesMove move) {
        /* Current player "player" and next "nextPlayer" */
        ManantialesPlayer player = (ManantialesPlayer) move.getPlayer();
        ManantialesPlayer nextPlayer = null;

        /* Set all players to no turn */
        for (GridPlayer p : game.getPlayers())
            p.setTurn(false);

        /* Determine who's next */
        nextPlayer = nextPlayer(game, player);

        /* Accounting required due to refactor */
        game.setTurns(game.getTurns() + 1);

        /* Give him the turn */
        nextPlayer.setTurn (true);
        return nextPlayer;
    }

     public static ManantialesPlayer nextPlayer(ManantialesGame game, ManantialesPlayer player) {
        int turns = game.getTurns() + 1;
        TreeSet<GridPlayer> sorted = new TreeSet<GridPlayer>(new PlayerComparator());
        sorted.addAll(game.getPlayers());
        ManantialesPlayer nextPlayer;

        /* Test if this is the start of a new round;
           modulus test must be met, and game must have no active conditions! */
        if ((turns % game.getMaxPlayers() == 0)) {
            int startingPlayerPos = ((turns / game.getMaxPlayers()) % game.getMaxPlayers());
            GridPlayer[] gps = sorted.toArray(new GridPlayer[game.getMaxPlayers()]);
            nextPlayer = (ManantialesPlayer) gps [ startingPlayerPos ];
        } else {
            if (sorted.last().equals(player))
                nextPlayer = (ManantialesPlayer) sorted.first();
            else {
                SortedSet<GridPlayer> tail = sorted.tailSet(player, false);
                nextPlayer = (ManantialesPlayer) tail.iterator().next();
            }
        }

        return nextPlayer;
    }

    /* Determines if player "first" is before player "second". */
    public static  boolean isPrecedingPlayer (ManantialesGame game, GridPlayer first, GridPlayer second) {
        ManantialesPlayer next = nextPlayer(game,(ManantialesPlayer) first);
        return (next.equals(second));
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
        HashSet<ManantialesFicha> deletions = new HashSet<ManantialesFicha>();

            /* Set up deletable types */
        HashSet<TokenType> deletables = new HashSet<TokenType>();
        deletables.add (TokenType.MODERATE_PASTURE);
        deletables.add(TokenType.INTENSIVE_PASTURE);

        switch (violation) {
            case NORTHERN_BORDER_DEFORESTED:
                for (GridCell cell : ret.getCells()) {
                    ManantialesFicha ficha = (ManantialesFicha) cell;
                    if (deletables.contains(ficha.getType()) && ficha.getBorder().equals(BorderType.NORTH))
                        deletions.add(ficha);
                }
                break;
            case WESTERN_BORDER_DEFORESTED:
                for (GridCell cell : ret.getCells()) {
                    ManantialesFicha ficha = (ManantialesFicha) cell;
                    if (deletables.contains(ficha.getType()) && ficha.getBorder().equals(BorderType.WEST))
                        deletions.add(ficha);
                }
                break;
            case SOUTHERN_BORDER_DEFORESTED:
                for (GridCell cell : ret.getCells()) {
                    ManantialesFicha ficha = (ManantialesFicha) cell;
                    if (deletables.contains(ficha.getType()) && ficha.getBorder().equals(BorderType.SOUTH))
                        deletions.add(ficha);
                }
                break;
            case EASTERN_BORDER_DEFORESTED:
                for (GridCell cell : ret.getCells()) {
                    ManantialesFicha ficha = (ManantialesFicha) cell;
                    if (deletables.contains(ficha.getType()) && ficha.getBorder().equals(BorderType.EAST))
                        deletions.add(ficha);
                }
                break;
            default:
                break;
        }

        for (ManantialesFicha ficha : deletions)
            ret.removeCell(ficha);

        return ret;
    }
    
    public static boolean isContiguous (AdjGraph graph, Cell a, Cell b) throws InvalidMoveException {
        int nodeA = graph.findNode(new Point (a.getRow(), a.getColumn()));
        int nodeB = graph.findNode(new Point (b.getRow(), b.getColumn()));
        if (nodeA == -1 || nodeB == -1)
            throw new InvalidMoveException ("Unable to find Node!");
        return graph.containsEdge(nodeA, nodeB); 
    }
    
    public static  GameGrid clearTerritory (ManantialesGame game, Color color) {
        GameGrid ret = game.getGrid();
        HashSet<ManantialesFicha> deletions = new HashSet<ManantialesFicha>();
        for (GridCell cell : ret.getCells()) {
            if (cell.getColor().equals(color)) {
                ManantialesFicha ficha = (ManantialesFicha) cell;
                if (ficha.getType().equals(TokenType.MANAGED_FOREST))
                    continue;
                deletions.add(ficha);
            }
        }

        for (ManantialesFicha ficha : deletions) {
            ret.removeCell (ficha);
        }

        return ret;
    }
}
