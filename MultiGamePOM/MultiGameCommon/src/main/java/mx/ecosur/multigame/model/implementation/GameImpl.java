/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
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
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.MessageSender;

public interface GameImpl extends Implementation {

	/**
	 * @return
	 */
	public int getId();

	/**
	 * @param registrant
	 * @return
	 * @throws InvalidRegistrationException 
	 */
	public GamePlayerImpl registerPlayer(RegistrantImpl registrant) throws InvalidRegistrationException;

	/**
	 * @param player
	 */
	public void removePlayer(GamePlayerImpl player);

	/**
	 * @param state
	 */
	public void setState(GameState state);

	/**
	 * @return
	 */
	public GameState getState();

	/**
	 * @param move
	 */
	public MoveImpl move(MoveImpl move) throws InvalidMoveException;

	/**
	 * @return
	 */
	public Set<MoveImpl> listMoves();

    /**
     * @param sender
     */
    public void setMessageSender (MessageSender sender);

    /**
     *
     * @return
     */
    public MessageSender getMessageSender ();

	/**
	 * @return
	 */
	public List<GamePlayer> listPlayers();

	/**
	 * @return
	 */
	public int getMaxPlayers();

	/**
	 * @param implementation
	 * @return
	 * @throws InvalidRegistrationException 
	 */
	public AgentImpl registerAgent(AgentImpl implementation) throws InvalidRegistrationException;


}
