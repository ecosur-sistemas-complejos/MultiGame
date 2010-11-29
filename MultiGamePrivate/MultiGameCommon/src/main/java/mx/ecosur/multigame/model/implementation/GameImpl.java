/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model.implementation;

import java.util.List;
import java.util.Set;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.exception.InvalidSuggestionException;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.MessageSender;

public interface GameImpl extends Implementation {

    public int getId();

    public GamePlayerImpl registerPlayer(RegistrantImpl registrant) throws InvalidRegistrationException;

    public void removePlayer(GamePlayerImpl player);

    public void setState(GameState state);

    public GameState getState();

    public MoveImpl move(MoveImpl move) throws InvalidMoveException;

    public Set<MoveImpl> listMoves();

    public SuggestionImpl suggest (SuggestionImpl suggestion) throws InvalidSuggestionException;

    public void setMessageSender (MessageSender sender);

    public MessageSender getMessageSender ();

    public List<GamePlayer> listPlayers();

    public int getMaxPlayers();

    public AgentImpl registerAgent(AgentImpl implementation) throws InvalidRegistrationException;

    public String getChangeSet();
}
