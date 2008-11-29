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
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.StrategyPlayer;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.pente.PenteStrategy;

public interface RegistrarInterface {

	/**
	 * Registers a player with the system, returning a color from the available
	 * list of colors, and registering the Player with the game of the specified
	 * type. This method throws an exception when a specific player has already
	 * been registered, or if the type of game no longer takes any players.
	 * 
	 * @param player
	 *            , color, type
	 * @return GamePlayer
	 * @throws InvalidRegistrationException
	 * @throws RemoteException
	 */
	//TODO: Delete this method is no longer necessary
	public abstract GamePlayer registerPlayer(Player player, Color color,
			GameType type) throws InvalidRegistrationException;

	/**
	 * Registers a player with the specified Game object. Allows greater control
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
	 * TODO: Make this generic.
	 */

	public abstract StrategyPlayer registerRobot(Game game, Player player,
			Color color, PenteStrategy strategy)
			throws InvalidRegistrationException;

	/**
	 * Unregisters a player from the system (when the Player quits playing the
	 * game).
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
	 * 
	 * @throws RemoteException
	 * 
	 */
	public abstract Player locatePlayer(String name);

	/**
	 * Simply creates a game with no players of the type, "type".
	 * 
	 * @param type
	 *            the type of game to create
	 * @return
	 */

	public abstract Game createGame(GameType type);

	/**
	 * Logs a player into the system based on their email. If the player does
	 * not exist then it is created.
	 * 
	 * @param email
	 * @return the persisted player
	 */
	public abstract Player login(String email);
	
	/**
	 * Gets the list of games that are not in state GameState.END for the current player
	 * 
	 * @param player the current player
	 * @return the list of games with the current players data included for each game.
	 * @see GameState
	 */
	public abstract List<Game> getUnfinishedGames(Player player);

}