/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.exception.InvalidSuggestionException;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.Implementation;
import mx.ecosur.multigame.model.implementation.MoveImpl;
import mx.ecosur.multigame.MessageSender;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Game implements Model {

    private static final long serialVersionUID = 4651502618321513050L;

    private GameImpl gameImpl;

    public Game() {
        super();
    }

    public Game (GameImpl gameImpl) {
        this.gameImpl = gameImpl;
    }

    public int getId() {
        return gameImpl.getId();
    }

    public GamePlayer registerPlayer(Registrant registrant) throws InvalidRegistrationException {
        return new GamePlayer(gameImpl.registerPlayer(registrant.getImplementation()));
    }

    public Agent registerAgent (Agent agent) throws InvalidRegistrationException {
        return new Agent (gameImpl.registerAgent (agent.getImplementation()));
    }

    public void removePlayer(GamePlayer player) {
        gameImpl.removePlayer (player.getImplementation());
    }

    public void setState(GameState state) {
        gameImpl.setState(state);
    }

    public GameState getState() {
        return gameImpl.getState();
    }

    public Move move(Move move) throws InvalidMoveException {
        return new Move (gameImpl.move(move.getImplementation()));
    }

    public Set<Move> getMoves() {
        Set<Move> collection = new HashSet<Move>();
        Set<MoveImpl> moves = gameImpl.listMoves();
        for (MoveImpl move : moves) {
                collection.add(new Move(move));
        }
        return collection;
    }

    public Suggestion suggest (Suggestion suggestion) throws InvalidSuggestionException {
        return new Suggestion (gameImpl.suggest(suggestion.getImplementation()));
    }

    public void setMessageSender (MessageSender sender) {
        gameImpl.setMessageSender (sender);
    }

    public MessageSender getMessageSender () {
        return gameImpl.getMessageSender();
    }
        
    public int getMaxPlayers () {
        return gameImpl.getMaxPlayers();
    }

    public List<GamePlayer> listPlayers () {
        return gameImpl.listPlayers();
    }

    public GameImpl getImplementation() {
        return gameImpl;
    }

    public void setImplementation(Implementation impl) {
        this.gameImpl = (GameImpl) impl;
    }

    public String getChangeSet () {
        return gameImpl.getChangeSet();
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;

        if (obj instanceof Game) {
            Game game = (Game) obj;
            ret = (game.getId() == getId());
        }

        return ret;
    }

@Override
    public int hashCode() {
        return new HashCodeBuilder().append(gameImpl.getId()).append(gameImpl).toHashCode();
    }

}
