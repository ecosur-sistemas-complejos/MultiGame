package mx.ecosur.multigame.ejb;

import java.rmi.RemoteException;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.ChatMessage;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;

import org.drools.FactException;
import org.drools.RuleBase;
import org.drools.StatefulSession;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SharedBoard implements SharedBoardLocal, SharedBoardRemote {

	@PersistenceContext(unitName = "MultiGame")
	private EntityManager em;

	private static Logger logger = Logger.getLogger(SharedBoard.class
			.getCanonicalName());

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardLocal#getGame(int)
	 */
	public Game getGame(int gameId) {
		return em.find(Game.class, gameId);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardLocal#initialize(int)
	 */
	public void initialize(int gameId) throws RemoteException {

		/*
		 * TODO: Since this is called multiple times it would be better to have
		 * a flag on the game do determine if it has been previously initialized
		 */

		try {

			logger.fine("Initializing game with id " + gameId);
			
			Game game = getGame(gameId);

			/*
			 * Create the StatefulSession, set the initial fact (game), focus on
			 * the initialization agenda group, and fire the rules
			 */
			RuleBase ruleBase = game.getType().getRuleBase();
			StatefulSession statefulSession = ruleBase.newStatefulSession(false);
			statefulSession.insert(game);
			statefulSession.setFocus("initialize");
			statefulSession.fireAllRules();
			statefulSession.dispose();
			
			logger.fine("Game with id " + gameId + " initialized");

		} catch (FactException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#getGameGrid(int)
	 */
	public GameGrid getGameGrid(int gameId) {
		
		logger.fine("Getting game grid for game with id " + gameId);
		
		Game game = getGame(gameId);
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

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#validateMove(mx.ecosur.multigame.ejb.entity.Move)
	 */
	public Move validateMove(Move move) throws InvalidMoveException,
			RemoteException {
		
		logger.fine("Validating move " + move);

		/* Obtain attached instance of game and associate with player */
		Game game = move.getPlayer().getGame();
		if (!em.contains(game))
			game = getGame(game.getId());
		else
			em.refresh(game);
		move.getPlayer().setGame(game);

		/* Validate the move against the rules */
		RuleBase ruleBase = game.getType().getRuleBase();
		StatefulSession statefulSession = ruleBase.newStatefulSession(false);
		statefulSession.insert(game);
		statefulSession.insert(move);
		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();

		if (move.getStatus() == Move.Status.INVALID){
			logger.fine("Move not valid : " + move);
			throw new InvalidMoveException("Invalid Move!");
		}
		statefulSession.dispose();

		return move;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#move(mx.ecosur.multigame.ejb.entity.Move)
	 */
	public void move(Move move) throws InvalidMoveException, RemoteException {
		
		logger.fine("Preparing to execute move " + move);

		/* Check that move has been validated */
		if (move.getStatus() != Move.Status.VERIFIED){
			logger.fine("Move not valid : " + move);
			throw new InvalidMoveException("Unverified or Invalid move!");
		}

		/* Obtain attached instance of game and player */
		GamePlayer player = move.getPlayer();
		if (!em.contains(player))
			player = em.find(player.getClass(), player.getId());
		/*
		 * TODO: I think that this could be cleaned up, if you have an attached
		 * player then player.getGame should always give an attached game if i
		 * understand jpa correctly?
		 */
		Game game = player.getGame();
		if (!em.contains(game))
			game = em.find(game.getClass(), game.getId());
		em.refresh(game);

		/* Refresh the move with the managed game and player */
		player.setGame(game);
		move.setPlayer(player);

		/* persist in order to define id */
		em.persist(move);

		/* Execute the move in the rules */
		RuleBase ruleBase = game.getType().getRuleBase();
		StatefulSession statefulSession = ruleBase.newStatefulSession(false);
		statefulSession.insert(move);
		statefulSession.insert(game);
		/*
		 * TODO: Message sender can be instantiated from the rules there is no
		 * reason to insert it
		 */
		statefulSession.insert(new MessageSender());

		statefulSession.setFocus("move");
		statefulSession.fireAllRules();

		statefulSession.setFocus("evaluate");
		statefulSession.fireAllRules();

		statefulSession.dispose();

		/* TODO: move this to the rules */
		incrementTurn(move.getPlayer());
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#incrementTurn(mx.ecosur.multigame.ejb.entity.GamePlayer)
	 */
	public GamePlayer incrementTurn(GamePlayer player) throws RemoteException {
		
		logger.fine("Incrementing turn for player " + player.getId());

		/* Check that player has turn */
		if (!player.isTurn())
			throw new RemoteException("Only the Player with the "
					+ "turn can increment the turn!");

		/* Get attached player and game */
		if (!em.contains(player))
			player = em.find(GamePlayer.class, player.getId());
		Game game = player.getGame();

		/* Remove turn from player */
		player.setTurn(false);

		/* Find next player */
		List<GamePlayer> players = game.getPlayers();
		int playerNumber = players.indexOf(player);
		GamePlayer nextPlayer = null;
		if (playerNumber == players.size() - 1) {
			nextPlayer = players.get(0);
		} else {
			nextPlayer = players.get(playerNumber + 1);
		}

		nextPlayer.setTurn(true);
		MessageSender messageSender = new MessageSender();
		messageSender.sendPlayerChange(game);

		return nextPlayer;

	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#getPlayers(int)
	 */
	public List<GamePlayer> getPlayers(int gameId) {
		logger.fine("Getting players for game with id: " + gameId);
		return getGame(gameId).getPlayers();
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#getMoves(int)
	 */
	public List<Move> getMoves(int gameId) {
		logger.fine("Getting moves for game with id: " + gameId);
		Game game = getGame(gameId);
		Query query = em.createNamedQuery(game.getType().getNamedMoveQuery());
		query.setParameter("game", game);
		return (List<Move>) query.getResultList();
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#updateMove(mx.ecosur.multigame.ejb.entity.Move)
	 */
	public Move updateMove(Move move) {
		logger.fine("Updating move with id: " + move.getId());
		
		/* Work around for bug in TopLink dealing with inherited object graphs */
		if (!em.contains (move.getPlayer())) {
			GamePlayer player = em.find(GamePlayer.class, move.getPlayer().getId());
			move.setPlayer(player);
		}
		
		em.merge(move);
		return move;
	}
	
	public void addMessage(ChatMessage chatMessage) {

		/* chat message sender may be a detatched entity */
		if (!em.contains(chatMessage.getSender())) {
			chatMessage.setSender(em.find(GamePlayer.class, chatMessage.getSender()
					.getId()));
		}

		em.persist(chatMessage);
	}

}
