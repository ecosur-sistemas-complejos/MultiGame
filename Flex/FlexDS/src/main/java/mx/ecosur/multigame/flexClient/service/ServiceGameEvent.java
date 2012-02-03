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
    
    private GridPlayer player;

    public ServiceGameEvent (GridGame game, GridPlayer player) {
        this.gameId = game.getId();
        this.player = player;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int id) {
        gameId = id;
    }

    public GridPlayer getPlayer() {
        return player;
    }

    public void setPlayer(GridPlayer player) {
        this.player = player;
    }
}
