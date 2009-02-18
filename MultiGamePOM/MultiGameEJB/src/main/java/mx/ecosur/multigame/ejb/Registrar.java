/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Registration is the process of adding or finding users in the system and
 * associating that user with a current or new game.  Token colors are 
 * determined dynamically, by the available colors per game.
 * 
 * @author awaterma@ecosur.mx
 */

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
import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.ejb.entity.pente.PentePlayer;
import mx.ecosur.multigame.ejb.entity.pente.StrategyPlayer;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.pente.PenteStrategy;

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
	 * Registers a player with a specific game.
	 * 
	 * @see mx.ecosur.multigame.ejb.RegistrarRemote#registerPlayer(mx.ecosur.multigame.ejb.entity.Game, mx.ecosur.multigame.ejb.entity.Player, mx.ecosur.multigame.Color)
	 */
	public GamePlayer registerPlayer(Game game, Player registrant, Color favoriteColor)
			throws InvalidRegistrationException 
	{
		if (!em.contains(game))
			game = em.find(Game.class, game.getId());
		
		registrant = locatePlayer(registrant.getName());

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
			game.addPlayer(player);
			em.persist(player);
			messageSender.sendPlayerChange(game);

			/* If is the last player to join the game then initialize the game */
			if (availColors.size() == 1) {

				RuleBase ruleBase = game.getType().getRuleBase();
				StatefulSession statefulSession = ruleBase
						.newStatefulSession();
				statefulSession.insert(game);
				statefulSession.setFocus("initialize");
				statefulSession.fireAllRules();
				statefulSession.dispose();
			}
		}

		return player;
		
	}
	
	
	/**
	 * Registers a robot with she specified Game object.
	 * 
	 * TODO:  Make this generic.
	 * @throws InvalidRegistrationException 
	 */
	
	public StrategyPlayer registerRobot (Game game, Player registrant, 
			Color favoriteColor, PenteStrategy strategy) throws InvalidRegistrationException 
	{
		if (!em.contains(game))
			game = em.find(Game.class, game.getId());
		
		registrant = locatePlayer(registrant.getName());
		
		StrategyPlayer player = new StrategyPlayer (game, registrant, favoriteColor, 
				strategy);
		
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
			game.addPlayer(player);
			messageSender.sendPlayerChange(game);

			/* If is the last player to join the game then initialize the game */
			if (availColors.size() == 1) {

				RuleBase ruleBase = game.getType().getRuleBase();
				StatefulSession statefulSession = ruleBase
						.newStatefulSession();
				statefulSession.insert(game);
				statefulSession.setFocus("initialize");
				statefulSession.fireAllRules();
				statefulSession.dispose();
			}
		}

		return player;
		
	}

	/**
	 * Registers a player into the System, and affiliates that player with the
	 * first available game that is waiting for more players.
	 * 
	 * @throws RemoteException
	 * @throws RemoteException
	 * 
	 */
	//TODO: delete.
	/*
	 * With new system for joining games by invitation the game is always specified when joining a game 
	 */
	public GamePlayer registerPlayer(Player registrant, Color favoriteColor,
			GameType type) throws InvalidRegistrationException {

		/* Load the registrant reference */
		if (!em.contains(registrant))
			registrant = locatePlayer(registrant.getName());
		
		/* Load the Game */
		Game game = locateGame(type);
		em.persist(game);
		return registerPlayer (game, registrant, favoriteColor);
	}

	/**
	 * Locates a game of the specified type waiting for more users.
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Game locateGame(GameType type) {
		Query query = em.createNamedQuery(type.getNamedQuery());
		query.setParameter("type", type);
		query.setParameter("state", GameState.WAITING);
		List<Game> games = query.getResultList();
		Game ret = null;
		if (games.size() == 0)
			ret = createNewGame(type);
		else
			ret = games.get(0);
		return ret;
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

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.RegistrarInterface#createGame(mx.ecosur.multigame.GameType)
	 */
	public Game createGame(GameType type) {
		return this.createNewGame(type);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.RegistrarInterface#login(java.lang.String)
	 */
	public Player login(String name) {
		Query query = em.createNamedQuery("getPlayer");
		query.setParameter("name", name);
		Player player;
		try {
			player = (Player) query.getSingleResult();
		} catch (NoResultException e) {
			player = createPlayer(name);
		} catch (NonUniqueResultException e) {
			throw new RuntimeException(
					"More than one player of that name found!");
		}
		return player;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.RegistrarInterface#getUnfinishedGames(mx.ecosur.multigame.ejb.entity.Player)
	 */
	@SuppressWarnings("unchecked")
	public List<Game> getUnfinishedGames(Player player) {
		Query query = em.createNamedQuery("getGamesByPlayer");
		query.setParameter("player", player);
		query.setParameter("state", GameState.END);
		return query.getResultList();
	}

}
