/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.interfaces;

import java.util.List;

import mx.ecosur.multigame.model.Agent;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Registrant;

import mx.ecosur.multigame.exception.InvalidRegistrationException;

public interface RegistrarInterface {
        
    /**
     * Creates a managed Registrant in the system
     */
    public Registrant register (Registrant registrant);

    /**
     * Registers a player with the specified Game object.
    * Returns the game wherein the player was registered
    * (for retrieving persistancy information and updating
    * state).
    *
     * @param game
     *              the game the agent is being registered to
     * @param player
    *      the registrant being registered
     * @return Game
     * @throws InvalidRegistrationException
     */
    public Game registerPlayer(Game game, Registrant player)
            throws InvalidRegistrationException;

    /**
     * Adds registers an agent with a specific game.
     *
     * @param game
     *              the game the agent is being registered with
     * @param agent
     *              the agent being registered
     */
    public Game registerAgent(Game game, Agent agent)
            throws InvalidRegistrationException;

    /**
     * Unregisters a player from the system (when the Registrant quits playing
     * the game).
     *
     * @param player
     * @throws InvalidRegistrationException
     */
    public Game unregister(Game game, GamePlayer player)
            throws InvalidRegistrationException;

    /**
     * Gets all the games that a player is in that are not in
     * the finished state
     *
     * @param player
     * @return List<Game>
     */
    public List<Game> getUnfinishedGames(Registrant player);

    /**
     * Gets all pending games that the specific player has not
     * joined.
     *
     * @param player
     * @return List<Game>
     */
    public List<Game> getPendingGames(Registrant player);
}
