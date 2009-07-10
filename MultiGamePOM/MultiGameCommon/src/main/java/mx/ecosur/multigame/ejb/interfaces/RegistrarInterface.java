/*
 * Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.interfaces;

import java.rmi.RemoteException;
import java.util.List;

import mx.ecosur.multigame.model.Agent;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Registrant;

import mx.ecosur.multigame.exception.InvalidRegistrationException;

public interface RegistrarInterface {
	
	/**
	 * Registers a player with the specified Game object.  Allows greater control
	 * over player registration with specific games.
	 * 
	 * @param repository
	 * 			  the persistent repository for the implemented game
	 * @param gqme
	 * 			 the game the agent is being registered to
	 * @param player
	 * @return
	 * @throws InvalidRegistrationException
	 */
	public GamePlayer registerAgent(Game game, Registrant player) 
		throws 
	InvalidRegistrationException;
	
	/**
	 * Adds registers an agent with a specific game.
	 * 
	 * @param game
	 * 		the game the agent is being registered with
	 * @param agent
	 * 		the agent being registered
	 */
	public GamePlayer registerAgent(Game game, Agent agent)
		throws 
	InvalidRegistrationException;

	/**
	 * Unregisters a player from the system (when the Registrant quits playing 
	 * the game).
	 * 
	 * @param player
	 * @throws InvalidRegistrationException 
	 * @throws RemoteException 
	 */
	public void unregisterPlayer(Game game, GamePlayer player) 
		throws 
	InvalidRegistrationException;

	/**
	 * Gets all the games that a player is in that are not in 
	 * the finished state
	 * 
	 * @param player
	 * 			the player
	 * @return
	 */
 	public List<Game> getUnfinishedGames(Registrant player);
 	
 	/**
 	 * Gets all pending games that the specific player has not
 	 * joined.
 	 * 
 	 * @param player
 	 * 			the player
 	 * @return
 	 */
 	public List<Game> getPendingGames(Registrant player);
}
