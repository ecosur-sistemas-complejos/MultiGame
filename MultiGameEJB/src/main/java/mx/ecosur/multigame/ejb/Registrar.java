package mx.ecosur.multigame.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.ejb.entity.pente.PentePlayer;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class Registrar implements RegistrarRemote, RegistrarLocal {

	@PersistenceContext(unitName = "MultiGame")
	public EntityManager em;

	private MessageSender messageSender;
	
	/**
	 * Default constructor 
	 */
	public Registrar() {
		super();
		messageSender = new MessageSender();
	}
	/**
	 * Registers a player into the System. Player registration consists of
	 * maintaining a stateful hash of all active games in the system, and
	 * player's registered with those games (for the handing out of available
	 * colors). The GAME entity bean is loaded by this method, and as player's
	 * join, the Games are updated with registered players.
	 * 
	 * @throws RemoteException
	 * @throws RemoteException
	 * 
	 */

	public GamePlayer registerPlayer(Player registrant, Color favoriteColor,
			GameType type)
		throws InvalidRegistrationException, RemoteException
	{
		
		/* Load the Game */
		Game game = locateGame(type);
		
		/* Load the registrant reference */
		if (!em.contains(registrant))
			registrant = locatePlayer (registrant.getName());	
		/*
		 * Update the player with the current time for registration 
		 */
		registrant.setLastRegistration(System.currentTimeMillis());
		
		/* Locate the game player */
		GamePlayer player =  locateGamePlayer (game, registrant, favoriteColor);

		if (!game.getPlayers().contains(player)) {			
			boolean colorAvailable = getAvailableColors(game).contains(player.getColor());
			
			if (!colorAvailable) {
				/*
				 * Pick a color from the list of available colors for the game
				 * type
				 */
				Iterator<Color> iter = getAvailableColors(game).iterator();
				if (!iter.hasNext()) {
					throw new InvalidRegistrationException(
							"No colors available, game full!");
				} else {
					Color color = iter.next();
					player.setColor(color);
				}
			}

			/* Add the new player to the game */
			try {
				game.addPlayer(player);
				messageSender.sendPlayerChange(game);
			} catch (RemoteException e) {
				throw new InvalidRegistrationException(e.getMessage());
			}
		}

		return player;
	}
	
	private GamePlayer locateGamePlayer (Game game, Player player, 
			Color favoriteColor) throws RemoteException 
	{
		GamePlayer ret;
		
		try {
			Query query = em.createNamedQuery(GamePlayer.getNamedQuery());
			query.setParameter("game",game);
			query.setParameter("player",player);
			ret = (GamePlayer) query.getSingleResult();
		} catch (EntityNotFoundException e) {
			throw new RemoteException ("Unable to find that GamePlayer!");
		} catch (NonUniqueResultException e) {
			throw new RemoteException ("More than one GamePlayer with that " +
					"name found!");
		} catch (NoResultException e) {
			switch (game.getType()) {
				case PENTE:
					ret = new PentePlayer (game, player, favoriteColor);
					break;
				default:
					ret = new GamePlayer (game, player, favoriteColor);
					break;
			}
		
			em.persist(ret);
		}
		
		return ret;
	}
	
	public List<Color> getAvailableColors(Game game) throws RemoteException {
		List<Color> colors =  getColors (game.getType());
		List<GamePlayer> players = game.getPlayers();
		for (GamePlayer player : players) {
			colors.remove (player.getColor());
		}

		return colors;
	}
	
	/*
	 * Returns the list of colors typically available for a specific
	 * GameType.
	 */
	private List<Color> getColors(GameType type) {

		List<Color> colors = new ArrayList<Color>();

		if (type == GameType.CHECKERS) {
			colors.add(Color.BLACK);
			colors.add(Color.RED);
		} else if (type == GameType.PENTE) {
			for (Color c : Color.values()) {
				if (c.equals(Color.UNKNOWN))
					continue;
				colors.add(c);
			}
		}
		
		return colors;
	}

	public Game locateGame(GameType type) throws RemoteException {
		Game game;

		/* Locate the game */
		try {
			Query query = em.createNamedQuery(type.getNamedQuery());
			query.setParameter("type", type);
			query.setParameter("state", GameState.END);
			game = (Game) query.getSingleResult();

		} catch (EntityNotFoundException e) {
			throw new RemoteException(e.getMessage());
		} catch (NonUniqueResultException e) {
			throw new RemoteException(e.getMessage());
		} catch (NoResultException e) {
			switch (type) {
				case PENTE:
					game = new PenteGame ();
					break;
				default:
					game = new Game();
					break;
			}
			
			game.initialize(type);
			em.persist(game);
		}
		return game;
	}

	public void unregisterPlayer(GamePlayer player)
			throws InvalidRegistrationException, RemoteException {
		/* Remove the user from the Game */
		Game game = player.getGame();
		if (!em.contains(game))
			game = em.find(game.getClass(), game.getId());
		/* refresh the game object */
		em.refresh(game);
		game.removePlayer(player);
		game.setState(GameState.END);
	}

	public Player locatePlayer(String name) throws RemoteException {
		Query query = em.createNamedQuery("getPlayer");
		query.setParameter("name", name);
		Player player;
		try {
			player = (Player) query.getSingleResult();
		} catch (EntityNotFoundException e) {
			player = new Player();
			player.setName(name);
		} catch (NonUniqueResultException e) {
			throw new RemoteException(
					"More than one player of that name found!");
		} catch (NoResultException e) {
			player = createPlayer(name);
		}

		return player;
	}

	public Game locateGame(GameType type, int id) throws RemoteException {
		Query query = em.createNamedQuery(type.getNamedQueryById());
		query.setParameter("id", id);
		Game game;

		try {
			game = (Game) query.getSingleResult();
		} catch (EntityNotFoundException e) {
			throw new RemoteException("Unable to find game with specified id!");
		} catch (NonUniqueResultException e) {
			throw new RemoteException(
					"More than one Game found!");
		} catch (NoResultException e) {
			throw new RemoteException("Unable to find game with specified id!");
		}

		return game;
	}

	/**
	 * @param name
	 * @return
	 */
	private Player createPlayer(String name) {
		Player player = new Player();
		player.setName(name);
		player.setGamecount(0);
		player.setWins(0);
		em.persist(player);
		return player;
	}
}
