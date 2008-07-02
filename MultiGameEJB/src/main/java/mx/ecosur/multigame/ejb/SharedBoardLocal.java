package mx.ecosur.multigame.ejb;


import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;

@Local
public interface SharedBoardLocal {
	
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
	public void locateSharedBoard (int gameId) throws RemoteException;
	
	/**
	 * Validates a specific move against a specific RuleBase
	 */
	public Move validateMove (Move move) throws InvalidMoveException;
	
	/**
	 * Makes the specified move on the game grid
	 */
	public void move (Move move) throws InvalidMoveException;
	
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
	 * Gets a Set of Dates that correspond to the times each message
	 * was added.
	 * @return
	 */
	public Set<Date> getMessageTimes ();

	/**
	 * Gets a Message at a specific datetime.
	 * @param date
	 * @return
	 */
	public String getMessage (Date date);
	
	/**
	 * Adds a message to the current message stream 
	 */
	public void addMessage(Player sender, String body, Date dateSent);
	
	/** 
	 * Increments the turn for GamePlay
	 */
	public void incrementTurn (Player player);
	
	/**
	 * Gets the Players playing on this shared board 
	 */
	public List<Player> getPlayers();
	
}
