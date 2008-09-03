package mx.ecosur.multigame.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.Remote;

import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.ejb.entity.ChatMessage;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;

@Remote
public interface SharedBoardRemote {

	/**
	 * Gets a game by its id
	 * 
	 * @param gameId
	 *            the id of the game
	 * @return
	 */
	public Game getGame(int gameId);

	/**
	 * Validates a specific move against a specific RuleBase. The move requires
	 * a player and a game.id property to allow the game to be retrieved.
	 * 
	 * @param move
	 * @return the move with its status updated
	 * @throws InvalidMoveException
	 * @throws RemoteException
	 */
	public Move validateMove(Move move) throws InvalidMoveException,
			RemoteException;

	/**
	 * Makes the specified move on the game grid. The move requires a player and
	 * a game.id property to allow the game to be retrieved.
	 * 
	 * @param move
	 * @throws InvalidMoveException
	 * @throws RemoteException
	 */
	public void move(Move move) throws InvalidMoveException, RemoteException;

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
	 * Increments the turn for GamePlay
	 * 
	 * Returns the player whose turn is next.
	 * 
	 * @param player
	 *            the player. This player should have its game.id property set.
	 * @return The player who has the turn after the turn is incremented.
	 * @throws RemoteException
	 */
	public GamePlayer incrementTurn(GamePlayer player) throws RemoteException;

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
