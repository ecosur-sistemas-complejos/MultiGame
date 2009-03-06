/*
 * Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb;

import java.rmi.RemoteException;
import java.util.List;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.StrategyPlayer;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.pente.PenteStrategy;

public interface RegistrarInterface {

	/**
	 * Registers a player with the system, returning a color from the
	 * available list of colors, and registering the Player with the game
	 * of the specified type.  This method throws an exception when a specific
	 * player has already been registered, or if the type of game no longer 
	 * takes any players.
	 * 
	 * @param player, color, type
	 * @return GamePlayer
	 * @throws InvalidRegistrationException 
	 * @throws RemoteException 
	 */
	public abstract GamePlayer registerPlayer(Player player, Color color,
			GameType type) throws InvalidRegistrationException;

	/**
	 * Registers a player with the specified Game object.  Allows greater control
	 * over player registration with specific games.
	 * 
	 * @param gqme
	 * @param player
	 * @param color
	 * @return
	 * @throws InvalidRegistrationException
	 */

	public abstract GamePlayer registerPlayer(Game game, Player player,
			Color color) throws InvalidRegistrationException;

	/**
	 * Registers a robot with she specified Game object.
	 * 
	 * TODO:  Make this generic.
	 */

	public abstract StrategyPlayer registerRobot(Game game, Player player,
			Color color, PenteStrategy strategy)
			throws InvalidRegistrationException;

	/**
	 * Unregisters a player from the system (when the Player quits playing 
	 * the game).
	 * 
	 * @param player
	 * @throws InvalidRegistrationException 
	 * @throws RemoteException 
	 */
	public abstract void unregisterPlayer(GamePlayer player)
			throws InvalidRegistrationException;

	/**
	 * Method to find the available token colors based on the gametype 
	 * requested.
	 * 
	 * @param type 
	 * @return A list of Colors that are still available
	 */
	public abstract List<Color> getAvailableColors(Game game)
			throws RemoteException;

	/**
	 * Locates a player 
	 * @throws RemoteException 
	 * 
	 */
	public abstract Player locatePlayer(String name);

	/**
	 * Attempt to locate an unfinished game of a given type and player. If no
	 * such game is found a game that requires more players to begin is searched
	 * for. If no game is found new game is created and returned.
	 * 
	 * @param player
	 *            the player to register
	 * @param type
	 *            the type of game
	 * @return the game.
	 * @throws RemoteException
	 */
	public abstract Game locateGame(Player player, GameType type);

	/**
	 * Simply creates a game with no players of the type, "type".
	 * 
	 * @param type
	 * 			the type of game to create
	 * @return
	 */
	
	public abstract Game createGame (GameType type);

	/**
	 * Logs a player into the platform. If the player doesn't exist
	 * it is automatically created
	 * 
	 * @param name
	 * 			the name of the player
	 * @return
	 */
	public abstract Player login(String name);

	/**
	 * Gets all the games that a player is in that are not in 
	 * the finished state
	 * 
	 * @param player
	 * 			the player
	 * @return
	 */
 	public abstract List<Game> getUnfinishedGames(Player player);
 	
 	/**
 	 * Gets all pending games that the specific player has not
 	 * joined.
 	 * 
 	 * @param player
 	 * 			the player
 	 * @return
 	 */
 	public abstract List<Game> getPendingGames(Player player);
}
