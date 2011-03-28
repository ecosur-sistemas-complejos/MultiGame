package mx.ecosur.multigame.flexClient.service;

import mx.ecosur.multigame.grid.model.GridGame;
import mx.ecosur.multigame.grid.model.GridPlayer;

/**
 * ServiceGameEvent.
 *
 * Returned to game based invokers of GameService; for registation, etc.
 *
 * Copyright 2009, Andrew Waterman and ECOSUR
 */
public class ServiceGameEvent {

    private GridGame game;

    private GridPlayer player;

    public ServiceGameEvent (GridGame game, GridPlayer player) {
        this.game = game;
        this.player = player;
    }

    public GridGame getGame() {
        return game;
    }

    public void setGame(GridGame game) {
        this.game = game;
    }

    public GridPlayer getPlayer() {
        return player;
    }

    public void setPlayer(GridPlayer player) {
        this.player = player;
    }
}
