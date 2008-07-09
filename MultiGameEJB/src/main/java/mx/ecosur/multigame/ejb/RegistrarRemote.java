/**
 * 
 */
package mx.ecosur.multigame.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.Remote;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Player;

/**
 * The RegistrarRemote interface is the remote interface for the
 * registrar statefull EJB, that manages gamestate and the registration
 * of new players into the gamespace.  
 * 
 * @author awater
 *
 */

@Remote
public interface RegistrarRemote {
	
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
	public GamePlayer registerPlayer (Player player, Color color, GameType type) 
		throws InvalidRegistrationException, RemoteException;
	
	/**
	 * Unregisters a player from the system (when the Player quits playing 
	 * the game).
	 * 
	 * @param player
	 * @throws InvalidRegistrationException 
	 * @throws RemoteException 
	 */
	public void unregisterPlayer (GamePlayer player) throws 
		InvalidRegistrationException, RemoteException;
	
	/**
	 * Method to find the available token colors based on the gametype 
	 * requested.
	 * 
	 * @param type 
	 * @return A list of Colors that are still available
	 */
	public List<Color> getAvailableColors (Game game) throws 
		RemoteException;
	
	/**
	 * Locates a player 
	 * @throws RemoteException 
	 * 
	 */
	public Player locatePlayer (String name) throws RemoteException;
	
	/**
	 * Locates a game
	 * @throws RemoteExeption
	 */
	public Game locateGame (GameType type) throws RemoteException;
	
}
