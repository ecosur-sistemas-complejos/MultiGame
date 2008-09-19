package mx.ecosur.multigame.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
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

import org.drools.RuleBase;
import org.drools.StatefulSession;

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
			GameType type) throws InvalidRegistrationException {

		/* Load the registrant reference */
		if (!em.contains(registrant))
			registrant = locatePlayer(registrant.getName());
		
		/* Load the Game */
		Game game = locateGame(registrant, type);

		/*
		 * Update the player with the current time for registration
		 */
		registrant.setLastRegistration(System.currentTimeMillis());

		/* Locate the game player */
		GamePlayer player = locateGamePlayer(game, registrant, favoriteColor);

		if (!game.getPlayers().contains(player)) {

			/*
			 * Get list of available colors left for this game and look for the
			 * players color
			 */
			List<Color> availColors = getAvailableColors(game);
			boolean colorAvailable = availColors.contains(player.getColor());

			if (!colorAvailable) {
				/*
				 * Pick a color from the list of available colors for the game
				 * type
				 */

				if (availColors.size() == 0) {
					throw new InvalidRegistrationException(
							"No colors available, game full!");
				} else {
					Color color = availColors.get(0);
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

			/* If is the last player to join the game then initialize the game */
			if (availColors.size() == 1) {

				RuleBase ruleBase = game.getType().getRuleBase();
				StatefulSession statefulSession = ruleBase
						.newStatefulSession(false);
				statefulSession.insert(game);
				statefulSession.setFocus("initialize");
				statefulSession.fireAllRules();
				statefulSession.dispose();
			}
		}

		return player;
	}

	private GamePlayer locateGamePlayer(Game game, Player player,
			Color favoriteColor) {
		GamePlayer ret;

		try {
			Query query = em.createNamedQuery(GamePlayer.getNamedQuery());
			query.setParameter("game", game);
			query.setParameter("player", player);
			ret = (GamePlayer) query.getSingleResult();
		} catch (EntityNotFoundException e) {
			throw new RuntimeException("Unable to find that GamePlayer!");
		} catch (NonUniqueResultException e) {
			throw new RuntimeException("More than one GamePlayer with that "
					+ "name found!");
		} catch (NoResultException e) {
			switch (game.getType()) {
			case PENTE:
				ret = new PentePlayer(game, player, favoriteColor);
				break;
			default:
				ret = new GamePlayer(game, player, favoriteColor);
				break;
			}

			em.persist(ret);
		}

		return ret;
	}

	public List<Color> getAvailableColors(Game game) {
		List<Color> colors = getColors(game.getType());
		List<GamePlayer> players = game.getPlayers();
		for (GamePlayer player : players) {
			colors.remove(player.getColor());
		}

		return colors;
	}

	/*
	 * Returns the list of colors typically available for a specific GameType.
	 */
	private List<Color> getColors(GameType type) {

		List<Color> colors = new ArrayList<Color>();

		if (type == GameType.CHECKERS) {
			colors.add(Color.YELLOW);
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


	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.RegistrarRemote#locateGame(mx.ecosur.multigame.ejb.entity.Player, mx.ecosur.multigame.GameType)
	 */
	public Game locateGame(Player player, GameType type) {
		Game game;
		Query query;

		try {
			/* Search for unfinished game with this player and type */
			query = em.createNamedQuery(type.getNamedQueryByTypeAndPlayer());
			query.setParameter("type", type);
			query.setParameter("player", player);
			query.setParameter("state", GameState.END);
			game = (Game) query.getSingleResult();

		} catch (NoResultException e) {
			
			/*
			 * User is not currently registered for any game. Search for any
			 * game in waiting state
			 */
			try {
				query = em.createNamedQuery(type.getNamedQuery());
				query.setParameter("type", type);
				query.setParameter("state", GameState.WAITING);
				game = (Game) query.getSingleResult();

			} catch (NoResultException e2) {

				/* No waiting game exists, create a new one and initialize it */
				game = createNewGame(type);
			} catch (NonUniqueResultException e2) {
				game = createNewGame (type);
			}
		}

		return game;
	}

	private Game createNewGame(GameType type) {
		Game game;
		switch (type) {
		case PENTE:
			game = new PenteGame();
			break;
		default:
			game = new Game();
			break;
		}
		game.initialize(type);
		em.persist(game);
		return game;
	}

	public void unregisterPlayer(GamePlayer player) throws InvalidRegistrationException {

		/* Remove the user from the Game */
		Game game = player.getGame();
		if (!em.contains(game))
			game = em.find(game.getClass(), game.getId());

		/* refresh the game object */
		em.refresh(game);
		game.removePlayer(player);
		game.setState(GameState.END);
	}

	public Player locatePlayer(String name) {
		Query query = em.createNamedQuery("getPlayer");
		query.setParameter("name", name);
		Player player;
		try {
			player = (Player) query.getSingleResult();
		} catch (EntityNotFoundException e) {
			player = new Player();
			player.setName(name);
		} catch (NonUniqueResultException e) {
			throw new RuntimeException(
					"More than one player of that name found!");
		} catch (NoResultException e) {
			player = createPlayer(name);
		}

		return player;
	}

	public Game locateGame(GameType type, int id) {
		Query query = em.createNamedQuery(type.getNamedQueryById());
		query.setParameter("id", id);
		Game game;

		try {
			game = (Game) query.getSingleResult();
		} catch (EntityNotFoundException e) {
			throw new RuntimeException("Unable to find game with specified id!");
		} catch (NonUniqueResultException e) {
			throw new RuntimeException("More than one Game found!");
		} catch (NoResultException e) {
			throw new RuntimeException("Unable to find game with specified id!");
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
