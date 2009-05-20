/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
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

import mx.ecosur.multigame.ejb.entity.ChatMessage;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.exception.InvalidMoveException;

public interface SharedBoardInterface {
	/**
	 * Gets a game by its id
	 * 
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
	public Move move(Move move) throws InvalidMoveException;

	/**
	 * Returns the a GameGrid object, populated with the current moves on the
	 * Grid. GameGrid's are read-only representations of grid state at a point
	 * in time.
	 * 
	 * @param gameId
	 *            the id of the game
	 * @return
	 */
	public GameGrid getGameGrid(int gameId);

	/**
	 * Gets the Players for a given game
	 * 
	 * @param gameId
	 *            the id of the game
	 * @return the players registered for this game
	 */
	public List<GamePlayer> getPlayers(int gameId);

	/**
	 * Gets all the moves for a given game.
	 * 
	 * @param gameId
	 *            the id of the game
	 * @return a list of all moves made in the game in the order that they were
	 *         made
	 */
	public List<Move> getMoves(int gameId);

	/**
	 * Updates a move
	 * 
	 * @param move
	 *            the move to update
	 * @return the updated move
	 */
	public Move updateMove(Move move);

	/**
	 * Adds a chat message
	 * 
	 * @param chatMessage
	 */
	public void addMessage(ChatMessage chatMessage);	

}
