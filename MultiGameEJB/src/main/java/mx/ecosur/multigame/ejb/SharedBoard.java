package mx.ecosur.multigame.ejb;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.GameEvent;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.ejb.entity.ChatMessage;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;

import org.drools.FactException;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;

@Stateful
public class SharedBoard implements SharedBoardRemote, SharedBoardLocal {
	
	@PersistenceContext (unitName="MultiGame")
	private EntityManager em;
	
	@Resource(mappedName="jms/TopicConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName="CHECKERS")
	private Topic topic; 
	
	private Game game;

	private RuleBase ruleset;

	private StatefulSession statefulSession;
	
	private static Logger logger = Logger.getLogger(
			SharedBoard.class.getCanonicalName());
	
	public SharedBoard () {
		game = null;
	}
	
	/**
	 * Locates a specific shared board of the GameType "type".
	 * 
	 * @param type
	 * @param player
	 * @throws RemoteException
	 */
	
	public void locateSharedBoard(GameType type) throws RemoteException {
		Query query = em.createQuery("select g from Game g where g.type=:type " +
				"and g.state <>:state");
		query.setParameter("type", type);
		query.setParameter("state", GameState.END);
		try {
			Game game = (Game) query.getSingleResult();
			this.setGame(game);
		} catch (EntityNotFoundException e) {
			throw new RemoteException("Unable to find game with specified id!");
		} catch (NonUniqueResultException e) {
			throw new RemoteException("More than one game of that type found!");
		} catch (NoResultException e) {
			createGame(type);
		}
	}
	
	public void locateSharedBoard(GameType type, Player player) throws RemoteException {
		Query query = em.createQuery("select g from Game as g, Player as player " +
				"where player=:player and player MEMBER OF g.players " +
				"and g.type=:type and g.state <>:state");
		query.setParameter("player", player);
		query.setParameter("type", type);
		query.setParameter("state", GameState.END);
		try {
			Game game = (Game) query.getSingleResult();
			this.setGame(game);
		} catch (EntityNotFoundException e) {
			throw new RemoteException("Unable to find game with specified id!");
		} catch (NonUniqueResultException e) {
			throw new RemoteException("More than one game of that type found!");
		} catch (NoResultException e) {
			createGame(type);
		}
	}	
	
	public void locateSharedBoard (int gameId) throws RemoteException {
		Query query = em.createQuery("select g from Game g where g.id=:id " +
		"and g.state<>:state");
		query.setParameter("id", gameId);
		query.setParameter("state", GameState.END);
		try {
			Game game = (Game) query.getSingleResult();
			this.setGame(game);
		} catch (EntityNotFoundException e) {
			throw new RemoteException("Unable to find running game with specified id!");
		} catch (NonUniqueResultException e) {
			throw new RemoteException("More than one game of that type found!");
		} catch (NoResultException e) {
			throw new RemoteException ("Unable to find running game with specified id!");
		}		
	}

	/**
	 * @param type
	 * @throws RemoteException
	 */
	void createGame(GameType type) throws RemoteException {
		/* Create a new Game */
		Game game = new Game();
		game.initialize(type);
		this.setGame(game);
	}
	
	/**
	 * Initializes a Game to the starting configuration.  (We can probably push
	 * more of this behavior into DRL if desired.  For now, the board dimensions
	 * are set and each individual game's rules are loaded, as well as the 
	 * initial fact (the game) is inserted and all initialization rules are
	 * fired (that agenda group is focused on prior to firing all rules)
	 * @throws IOException 
	 * @throws DroolsParserException 
	 * 
	 * @throws Exception 
	 */
	void initialize() throws RemoteException 
	{	
		try {
			/* Initialize the rules */
			PackageBuilder builder = new PackageBuilder();
			InputStreamReader reader = null;
				
			switch (game.getType()) {
				case CHECKERS:
					reader = new InputStreamReader( 
						this.getClass().getResourceAsStream(
							"/mx/ecosur/multigame/checkers.drl"));
					builder.addPackageFromDrl(reader);
					break;
				case PENTE:
					reader = new InputStreamReader(
						this.getClass().getResourceAsStream(
								"/mx/ecosur/multigame/pente.drl"));
					builder.addPackageFromDrl(reader);
					break;
				default:
					break;
			}
			
			if (reader != null)
				reader.close();
			
				/* Create the StatefulSession, push the game into working memory,
				 * set the agenda group to "initialize" and start the game 
				 */
			 ruleset = RuleBaseFactory.newRuleBase();
			 ruleset.addPackage( builder.getPackage() );
			 
			 statefulSession = ruleset.newStatefulSession(false);
			 
			 	/* Set the initial fact (game), and focus on the initialization
			 	 * agenda group 
			 	 */
			 statefulSession.insert(game);
			 statefulSession.setFocus("initialize");
			 statefulSession.fireAllRules();
			 statefulSession.dispose();
		} catch (FactException e) {
			e.printStackTrace();
			throw new RemoteException (e.getMessage());
		} catch (DroolsParserException e) {
			e.printStackTrace();
			throw new RemoteException (e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RemoteException (e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException (e.getMessage());
		}
	}

	/**
	 * Returns a read-only game grid to the caller 
	 * @throws CloneNotSupportedException 
	 */
	public GameGrid getGameGrid() {
		List<Cell> ret = new ArrayList<Cell> (); 
		
		try {
			for (Cell c : game.getGrid().getCells()) {
				Cell cell = c.clone();
				ret.add(cell);
			}
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException ("Unexpected CloneException from " +
					"cell cloning operation!");
		}
		
		return new GameGrid (ret);
	}

	
	public GameType getGameType() {
		return game.getType();
	}

	public GameState getState() {
		return game.getState();
	}

	/**
	 * Uses the Drools engine to validate a specific move on the 
	 * shared board
	 * 
	 *  @throws InvalidMoveException
	 */
	public Move validateMove(Move move) throws InvalidMoveException {
		statefulSession = ruleset.newStatefulSession();
		statefulSession.insert(game);
		statefulSession.insert(move);
		
		statefulSession.setFocus ("verify");
		statefulSession.fireAllRules();
		
		if (move.getStatus() == Move.Status.INVALID)
			throw new InvalidMoveException ("Invalid Move!");
		statefulSession.dispose();
		
		return move;
	}
	
	/**
	 * Applies the validated move to the game grid.
	 * @throws RemoteException 
	 */
	public void move (Move move) throws InvalidMoveException, RemoteException {
		if (move.getStatus() != Move.Status.VERIFIED)
			throw new InvalidMoveException ("Unverified or Invalid move!");
		if (!em.contains(game))
			game = em.merge(game);
		if (!em.contains(move))
			move = em.merge(move);
		
		statefulSession = ruleset.newStatefulSession();
		statefulSession.insert(game);
		statefulSession.insert(move);
		
		statefulSession.setFocus ("move");
		statefulSession.fireAllRules();
		statefulSession.dispose();
		
		/* Persist the move */
		em.persist(game);
		em.persist(move);
		
		incrementTurn(move.getPlayer());
	}

	public Dimension getSize() {
		return game.getSize();
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) throws RemoteException {
		if (game == null)
			throw new RemoteException ("Attempting to set null game into SharedBoard!");
		this.game = em.merge(game);
		initialize();
	}
	
	public Set<Date> getMessageTimes () {
		return game.getMessageTimes();
	}

	public String getMessage (Date date) {
		return game.getMessage(date);
	}

	public void addMessage(Player sender, String body, Date dateSent) {
		ChatMessage cm = new ChatMessage();
		cm.setSender(sender);
		cm.setBody(body);
		cm.setDateSent(dateSent);
		em.persist(cm);
	}

	public Player incrementTurn(Player player) throws RemoteException {
		if (!player.isTurn())
			throw new RemoteException ("Only the Player with the " +
					"turn can increment the turn!");
		if (!em.contains(player))
			player = em.merge(player);
		player.setTurn(false);
		em.persist(player);
		
		List<Player> players = game.getPlayers();
		int playerNumber = players.indexOf(player);
		Player nextPlayer = null;
		if (playerNumber == players.size() - 1) {
			nextPlayer = players.get(0);
		} else {
			nextPlayer = players.get(playerNumber + 1);
		}

		if (!em.contains(nextPlayer))
			nextPlayer = em.merge(nextPlayer);
		nextPlayer.setTurn(true);
		em.persist(nextPlayer);
		sendGameEvent(GameEvent.TURN);
		
		return nextPlayer;
		
	}
	
	private void sendGameEvent(GameEvent event){
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

	public List<Player> getPlayers() {
		return game.getPlayers();
	}	

}
