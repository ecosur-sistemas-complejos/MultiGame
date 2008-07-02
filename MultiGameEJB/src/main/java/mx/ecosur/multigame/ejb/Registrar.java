package mx.ecosur.multigame.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameEvent;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.Player;

@Stateful
public class Registrar implements RegistrarRemote, RegistrarLocal {

	@PersistenceContext(unitName = "MultiGame")
	public EntityManager em;
	
	@Resource(mappedName="jms/TopicConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName="CHECKERS")
	private Topic topic; 

	Map<GameType, List<Color>> availableColors;

	public Registrar() {
		availableColors = new HashMap<GameType, List<Color>>();

		/* Populate the initial lists of available colors per game type */
		initColors(GameType.CHECKERS);
		initColors(GameType.PENTE);
	}

	public void initColors(GameType type) {

		List<Color> colors = new ArrayList<Color>();

		if (type == GameType.CHECKERS) {
			colors.add(Color.BLACK);
			colors.add(Color.RED);
		} else if (type == GameType.PENTE) {
			colors.add(Color.BLACK);
			colors.add(Color.BLUE);
			colors.add(Color.GREEN);
			colors.add(Color.RED);
		}
		
		availableColors.remove(type);
		availableColors.put(type, colors);
	}

	public List<Color> getAvailableColors(GameType type) throws RemoteException {
		List<Color> colors = availableColors.get(type);
		Game game = locateGame(type);
		List<Player> players = game.getPlayers();
		ListIterator<Player> iter = players.listIterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			colors.remove(p.getColor());
		}

		return colors;
	}

	/**
	 * Registers a player into the System. Player registration consists of
	 * maintaining a stateful hash of all active games in the system, and
	 * player's registered with those games (for the handing out of available
	 * colors). The GAME entity bean is loaded by this method, and as player's
	 * join, the Games are updated with registered pleayers.
	 * 
	 * @throws RemoteException
	 * @throws RemoteException
	 * 
	 */

	public Color registerPlayer(Player playerDAO, GameType type)
			throws InvalidRegistrationException, RemoteException {

		Color ret = playerDAO.getColor();

		/*
		 * Use the EM to pull a non-detached Player from the Store, update that
		 * player with the current registration time and settings from the
		 * PlayerDAO, and persist the entity into the store.
		 * 
		 * Catch the RemoteException for the case of a new Player.
		 */
		Player player;

		try {
			player = this.locatePlayer(playerDAO.getName());
		} catch (RemoteException e) {
			player = new Player();
			player.setName(playerDAO.getName());
		}

		/* Load the Game */
		Game game = locateGame(type);

		if (!game.getPlayers().contains(player)) {
			boolean colorAvailable = getAvailableColors(type).contains(
					playerDAO.getColor());
			if (colorAvailable) {
				ret = playerDAO.getColor();
			} else if (!colorAvailable) {
				/*
				 * Pick a color from the list of available colors for the game
				 * type
				 */
				Iterator<Color> iter = getAvailableColors(type).iterator();
				if (!iter.hasNext()) {
					throw new InvalidRegistrationException(
							"No colors available, game full!");
				} else {
					Color color = iter.next();
					playerDAO.setColor(color);
					ret = color;
				}
			}

			/* Set the new player's color */
			player.setColor(ret);

			/* Add the new player to the game */
			try {
				game.addPlayer(player);
			} catch (RemoteException e) {
				throw new InvalidRegistrationException(e.getMessage());
			}
			
			//if is the last player to join the game can begin
			if (getAvailableColors(type).size() == 0){
				sendGameEvent(game, GameEvent.BEGIN);
			}
			
		} else {
			ret = player.getColor();
		}

		/*
		 * Update the player with the current time for registration and persist
		 * the individual into the system.
		 */
		player.setLastRegistration(System.currentTimeMillis());

		/* Persist the game and the player */
		em.persist(player);
		em.persist(game);

		return ret;
	}

	public Game locateGame(GameType type) throws RemoteException {
		Game game;

		/* Locate the game */
		try {
			Query query = em
					.createQuery("select g from Game g where g.type=:type and g.state<>:state");
			query.setParameter("type", type);
			query.setParameter("state", GameState.END);
			game = (Game) query.getSingleResult();

		} catch (EntityNotFoundException e) {
			throw new RemoteException(e.getMessage());
		} catch (NonUniqueResultException e) {
			throw new RemoteException(e.getMessage());
		} catch (NoResultException e) {
			game = new Game();
			game.initialize(type);
		}
		return game;
	}

	public void unregisterPlayer(Player player, GameType type)
			throws InvalidRegistrationException, RemoteException {
		/* Remove the user from the Game */
		Game game = locateGame(type);
		if (game.getId() > 0) {
			game.removePlayer(player);
			game.setState(GameState.END);
			initColors(type);
		}
		
		game.removePlayer(player);
		
		em.persist(game);
	}

	public Player locatePlayer(String name) throws RemoteException {
		Query query = em
				.createQuery("select p from Player p where p.name=:name");
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

	public Game locateGame(int id) throws RemoteException {
		Query query = em.createQuery("select g from Game g where g.id=:id");
		query.setParameter("id", id);
		Game game;

		try {
			game = (Game) query.getSingleResult();
		} catch (EntityNotFoundException e) {
			throw new RemoteException("Unable to find game with specified id!");
		} catch (NonUniqueResultException e) {
			throw new RemoteException(
					"More than one player of that name found!");
		} catch (NoResultException e) {
			throw new RemoteException("Unable to find game with specified id!");
		}

		return game;
	}
	
	private void sendGameEvent(Game game, GameEvent event){
		try {
			Connection connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(topic);
			MapMessage message = session.createMapMessage();
			message.setIntProperty("GAME_ID", game.getId());
			message.setStringProperty("GAME_EVENT", event.toString());
			producer.send(message);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
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
