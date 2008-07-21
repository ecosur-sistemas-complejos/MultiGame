package mx.ecosur.multigame.ejb;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.ejb.entity.ChatMessage;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;

import org.drools.FactException;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;

@Stateful
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SharedBoard implements SharedBoardRemote, SharedBoardLocal {

	@PersistenceContext(unitName = "MultiGame")
	private EntityManager em;

	private Game game;

	private RuleBase ruleset;

	private StatefulSession statefulSession;

	private MessageSender messageSender;

	private static Logger logger = Logger.getLogger(SharedBoard.class
			.getCanonicalName());

	public SharedBoard() {
		game = null;
		messageSender = new MessageSender();
	}

	/**
	 * Locates a specific shared board of the GameType "type".
	 * 
	 * @param type
	 * @param player
	 * @throws RemoteException
	 */

	public void locateSharedBoard(GameType type) throws RemoteException {
		Query query = em.createNamedQuery(type.getNamedQuery());
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

	public void locateSharedBoard(GameType type, int gameId)
			throws RemoteException {
		Query query = em.createNamedQuery(type.getNamedQueryById());
		query.setParameter("id", gameId);
		query.setParameter("state", GameState.END);
		try {
			Game game = (Game) query.getSingleResult();
			this.setGame(game);
		} catch (EntityNotFoundException e) {
			throw new RemoteException("Unable to find running game with "
					+ "specified id!");
		} catch (NonUniqueResultException e) {
			throw new RemoteException("More than one game of that type found!");
		} catch (NoResultException e) {
			throw new RemoteException("Unable to find running game with "
					+ "specified id!");
		}
	}

	/**
	 * @param type
	 * @throws RemoteException
	 */
	void createGame(GameType type) throws RemoteException {
		/* Create a new Game */
		switch (type) {
		case PENTE:
			PenteGame pg = new PenteGame();
			pg.initialize(type);
			this.setGame(pg);
			break;
		default:
			Game rg = new Game();
			rg.initialize(type);
			this.setGame(rg);
		}
	}

	/**
	 * Initializes a Game to the starting configuration. (We can probably push
	 * more of this behavior into DRL if desired. For now, the board dimensions
	 * are set and each individual game's rules are loaded, as well as the
	 * initial fact (the game) is inserted and all initialization rules are
	 * fired (that agenda group is focused on prior to firing all rules)
	 * 
	 * @throws IOException
	 * @throws DroolsParserException
	 * 
	 * @throws Exception
	 */
	void initialize() throws RemoteException {
		try {
			/* Initialize the rules */
			PackageBuilder builder = new PackageBuilder();
			InputStreamReader reader = null;

			switch (game.getType()) {
			case CHECKERS:
				reader = new InputStreamReader(this.getClass()
						.getResourceAsStream(
								"/mx/ecosur/multigame/checkers.drl"));
				builder.addPackageFromDrl(reader);
				break;
			case PENTE:
				reader = new InputStreamReader(this.getClass()
						.getResourceAsStream("/mx/ecosur/multigame/gente.drl"));
				builder.addPackageFromDrl(reader);
				break;
			default:
				break;
			}

			if (reader != null)
				reader.close();

			/*
			 * Create the StatefulSession, push the game into working memory,
			 * set the agenda group to "initialize" and start the game
			 */
			ruleset = RuleBaseFactory.newRuleBase();
			ruleset.addPackage(builder.getPackage());
			statefulSession = ruleset.newStatefulSession(false);

			/*
			 * Set the initial fact (game), and focus on the initialization
			 * agenda group
			 */
			statefulSession.insert(game);
			statefulSession.setFocus("initialize");
			statefulSession.fireAllRules();
			statefulSession.dispose();
		} catch (FactException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		} catch (DroolsParserException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	/**
	 * Returns a read-only game grid to the caller
	 * 
	 * @throws CloneNotSupportedException
	 */
	public GameGrid getGameGrid() {
		game = em.find(Game.class, game.getId());
		TreeSet<Cell> ret = new TreeSet<Cell>(new CellComparator());
		try {
			for (Cell c : game.getGrid().getCells()) {
				Cell cell = c.clone();
				ret.add(cell);
			}
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Unexpected CloneException from "
					+ "cell cloning operation!");
		}

		return new GameGrid(ret);
	}

	public GameType getGameType() {
		return game.getType();
	}

	public GameState getState() {
		return game.getState();
	}

	/**
	 * Uses the Drools engine to validate a specific move on the shared board
	 * 
	 * @throws InvalidMoveException
	 * @throws RemoteException
	 */
	public Move validateMove(Move move) throws InvalidMoveException {
		Game game = move.getPlayer().getGame();
		if (!em.contains(game))
			game = em.find(game.getClass(), game.getId());
		else
			em.refresh(game);
		move.getPlayer().setGame(game);

		statefulSession = ruleset.newStatefulSession();
		statefulSession.insert(game);
		statefulSession.insert(move);

		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();

		if (move.getStatus() == Move.Status.INVALID)
			throw new InvalidMoveException("Invalid Move!");
		statefulSession.dispose();

		return move;
	}

	/**
	 * Applies the validated move to the game grid.
	 * 
	 * @throws RemoteException
	 */
	public void move(Move move) throws InvalidMoveException, RemoteException {
		if (move.getStatus() != Move.Status.VERIFIED)
			throw new InvalidMoveException("Unverified or Invalid move!");
		Game game = move.getPlayer().getGame();
		if (!em.contains(game))
			game = em.find(game.getClass(), game.getId());
		em.refresh(game);

		//persist in order to define id
		em.persist(move);
		
		statefulSession = ruleset.newStatefulSession();
		statefulSession.insert(game);
		statefulSession.insert(move);
		statefulSession.insert(messageSender);

		statefulSession.setFocus("move");
		statefulSession.fireAllRules();
		statefulSession.dispose();

		

		/* TODO: This could be moved out to the rules */
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
			throw new RemoteException(
					"Attempting to set null game into SharedBoard!");
		this.game = em.merge(game);
		initialize();
	}

	public void addMessage(ChatMessage chatMessage) {

		/* chat message sender may be a detatched entity */
		if (!em.contains(chatMessage.getSender())) {
			chatMessage.setSender(em.find(Player.class, chatMessage.getSender()
					.getId()));
		}

		em.persist(chatMessage);
	}

	public GamePlayer incrementTurn(GamePlayer player) throws RemoteException {
		if (!player.isTurn())
			throw new RemoteException("Only the Player with the "
					+ "turn can increment the turn!");
		if (!em.contains(player))
			player = em.find(GamePlayer.class, player.getId());
		player.setTurn(false);

		if (!em.contains(game))
			game = em.find(Game.class, game.getId());

		List<GamePlayer> players = game.getPlayers();
		int playerNumber = players.indexOf(player);
		GamePlayer nextPlayer = null;
		if (playerNumber == players.size() - 1) {
			nextPlayer = players.get(0);
		} else {
			nextPlayer = players.get(playerNumber + 1);
		}

		nextPlayer.setTurn(true);
		messageSender.sendPlayerChange(game);

		return nextPlayer;

	}

	public List<GamePlayer> getPlayers() {
		return game.getPlayers();
	}
	
	@SuppressWarnings("unchecked")
	public List<Move> getMoves() {
		Query query = em.createNamedQuery(game.getType().getNamedMoveQuery());
		query.setParameter("game", game);
		return query.getResultList();
	}
	
	public Move updateMove(Move move){
		em.merge(move);
		return move;
	}

}
