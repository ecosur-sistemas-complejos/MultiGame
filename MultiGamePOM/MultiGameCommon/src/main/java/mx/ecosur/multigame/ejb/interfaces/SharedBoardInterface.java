/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.interfaces;

import java.rmi.RemoteException;
import java.util.Collection;

import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.model.ChatMessage;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.Move;

public interface SharedBoardInterface {
	/**
	 * Gets a game by its id
	 * 
	 * @param repository
	 * 			  the persistent repository for the game
	 * @param gameId
	 *            the id of the game
	 * @return
	 */
	public Game getGame(int gameId);

	/**
	 * Makes the specified move on the game grid and invokes the set of game
	 * rule's affiliated with the Move's game.  
	 * 
	 * @param move
	 * @throws InvalidMoveException
	 * @throws RemoteException
	 */
	public Move move (Game game, Move move) throws InvalidMoveException;

	/**
	 * Gets all the moves for a given game.
	 * 
	 * @param repository
	 * 			  the persistent repository for the game	 
	 * @param gameId
	 *            the id of the game
	 * @return a list of all moves made in the game in the order that they were
	 *         made
	 */
	public Collection<Move> getMoves(int gameId);

	/**
	 * Adds a chat message
	 * 
	 * @param repository
	 * 			  the persistent repository for the game
	 * @param chatMessage
	 */
	public void addMessage(ChatMessage chatMessage);	
	
	/**
	 * Updates a move (with user input information, for example)
	 * 
	 * @param move
	 * 			the move to be updated.
	 */
	public Move updateMove (Move move);

}
