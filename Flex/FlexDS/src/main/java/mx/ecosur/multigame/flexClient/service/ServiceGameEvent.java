package mx.ecosur.multigame.flexClient.service;

import mx.ecosur.multigame.grid.entity.GridGame;
import mx.ecosur.multigame.grid.entity.GridPlayer;

/**
 * ServiceGameEvent.
 *
 * Returned to game based invokers of GameService; for registation, etc.
 *
 * Copyright 2009, Andrew Waterman and ECOSUR
 */
public class ServiceGameEvent {

    private int gameId;
    
    private String gameType;
    
    private GridGame game;
    
    private GridPlayer player;

    public ServiceGameEvent (GridGame game, GridPlayer player) {
        this.gameId = game.getId();
        this.gameType = game.getGameType();
        this.game = game;
        this.player = player;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int id) {
        gameId = id;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public GridGame getGame() {
        return game;        
    }
    
    public void setGame(GridGame g) {
        this.game = g;
    }

    public GridPlayer getPlayer() {
        return player;
    }

    public void setPlayer(GridPlayer player) {
        this.player = player;
    }
}
