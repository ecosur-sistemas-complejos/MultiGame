package mx.ecosur.multigame.flexClient.dao;

import mx.ecosur.multigame.grid.entity.GridGame;
import mx.ecosur.multigame.grid.entity.GridPlayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 */
public class GameDAO {
    
    private int gameId;
    
    private Date creationDate;
    
    private String gameType;
    
    private List<String> players;
    
    private String status;
    
    public GameDAO () {
        super();
    }
    
    public GameDAO (GridGame game) {
        this();
        this.gameId = game.getId();
        this.creationDate = game.getCreated();
        this.gameType = game.getGameType();
        this.status = game.getState().name();
        this.players = new ArrayList<String>();
        for (GridPlayer p : game.getPlayers()) {
            this.players.add(p.getName());
            System.out.println("Adding player to DAO: " + p.getName());
        }
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
