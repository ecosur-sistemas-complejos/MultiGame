/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model.interfaces;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.exception.InvalidSuggestionException;
import mx.ecosur.multigame.MessageSender;

public interface Game extends Serializable {

    public int getId();

    public GamePlayer registerPlayer(Registrant registrant) throws InvalidRegistrationException;

    public void removePlayer(GamePlayer player);

    public void setState(GameState state);

    public GameState getState();

    public Move move(Move move) throws InvalidMoveException;

    public Set<Move> listMoves();

    public Suggestion suggest (Suggestion suggestion) throws InvalidSuggestionException;

    public void setMessageSender (MessageSender sender);

    public MessageSender getMessageSender ();

    public List<GamePlayer> listPlayers();

    public int getMaxPlayers();

    public Agent registerAgent(Agent implementation) throws InvalidRegistrationException;

    public String getChangeSet();
}
