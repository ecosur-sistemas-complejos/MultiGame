package mx.ecosur.multigame.ejb;


import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.ejb.entity.ChatMessage;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;

@Remote
public interface SharedBoardRemote {
	
	/**
	 * Locates a game of the specified type, and updates the 
	 * current handle with that game's information.
	 * @throws RemoteException 
	 * @TODO Expand this later to search for a game with specific players 
	 * 
	 * 
	 */
	public void locateSharedBoard (GameType type) throws RemoteException;
	
	/**
	 * Locates a game with the specified id. 
	 */
	public void locateSharedBoard (GameType type, int gameId) throws RemoteException;
	
	/**
	 * Validates a specific move against a specific RuleBase
	 */
	public Move validateMove (Move move) throws InvalidMoveException;
	
	/**
	 * Makes the specified move on the game grid
	 * @throws RemoteException 
	 */
	public void move (Move move) throws InvalidMoveException, RemoteException;
	
	/**
	 * Returns the a GameGrid object, populated with the current
	 * moves on the Grid.  GameGrid's are read-only representations
	 * of grid state at a point in time. 
	 * @throws CloneNotSupportedException 
	 */
	public GameGrid getGameGrid ();
	
	/**
	 * Returns the current state of the Game managed by this EJB
	 */
	public GameState getState ();
	
	/**
	 * Returns the current Game managed by this EJB
	 */
	public Game getGame ();
	
	/**
	 * Returns the type of game this shared board manages
	 */
	public GameType getGameType (); 
	
	/**
	 * Returns the size of the shared board 
	 */
	public Dimension getSize ();
	
	/** 
	 * Increments the turn for GamePlay
	 * 
	 * Returns the player whose turn is next.
	 * 
	 * @throws RemoteException 
	 */
	public GamePlayer incrementTurn (GamePlayer player) throws RemoteException;
	
	/**
	 * Gets the Players playing on this shared board 
	 */
	public List<GamePlayer> getPlayers();
	
	/**
	 * Adds a message to the current message stream 
	 */
	public void addMessage(ChatMessage chatMessage);	
	
	/**
	 * Gets all the moves made on this shared board 
	 */
	public List<Move> getMoves();
	
}
