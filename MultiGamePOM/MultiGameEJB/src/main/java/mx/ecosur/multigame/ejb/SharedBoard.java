/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * The SharedBoardEJB handles operations between players and the shared game
 * board.  The SharedBoardEJB manges game specific events, such as validating a
 * specific move on a game board, making a specific move, and modifying a 
 * previous move.  Clients can also add chat messages to the message stream,
 * increment players turns (soon to be phased into the game rules), and get
 * a list of players for a specific game.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.ejb;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.ChatMessage;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.exception.InvalidMoveException;

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
	public void initialize(int gameId) {

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
			StatefulSession statefulSession = ruleBase.newStatefulSession();
			statefulSession.insert(game);
			statefulSession.setFocus("initialize");
			statefulSession.fireAllRules();
			statefulSession.dispose();
			
			logger.fine("Game with id " + gameId + " initialized");

		} catch (FactException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
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
		return game.getGrid();
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#move(mx.ecosur.multigame.ejb.entity.Move)
	 */
	public Move move(Move move) throws InvalidMoveException {		
		logger.fine("Preparing to execute move " + move);

		/* Obtain attached instance of game and player */
		GamePlayer player = move.getPlayer();
		if (!em.contains(player))
			player = em.find(player.getClass(), player.getId());

		/* Refresh the move with the managed game and player */
		move.setPlayer(player);

		/* persist in order to define id */
		Cell current = move.getCurrent();
		if (current != null)
			move.setCurrent (em.find (current.getClass(), current.getId()));
		em.persist(move);		
		
		Game game = player.getGame();

		/* Execute the move in the rules */
		RuleBase ruleBase = game.getType().getRuleBase();
		StatefulSession statefulSession = ruleBase.newStatefulSession();
		
		/* Insert all known information into working memory */
		statefulSession.insert(move);
		statefulSession.insert(game);
		for (Object fact : game.getFacts()) 
			statefulSession.insert(fact);
		
		lifecycle(statefulSession);
		
			/* Merge all changes */
		em.merge(move);
		em.merge(game);		
		
		if (move.getStatus().equals(Move.Status.INVALID))
			throw new InvalidMoveException ("INVALID Move.");			
		return move;
	}

	private void lifecycle(StatefulSession statefulSession) {
		/* Run through the active lifecycle */
		statefulSession.setFocus("verify");
		statefulSession.fireAllRules();
		statefulSession.setFocus("move");
		statefulSession.fireAllRules();
		statefulSession.setFocus("evaluate");
		statefulSession.fireAllRules();
		statefulSession.dispose();
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
	@SuppressWarnings("unchecked")
	public List<Move> getMoves(int gameId) {
		logger.fine("Getting moves for game with id: " + gameId);
		Game game = getGame(gameId);
		Query query = em.createNamedQuery(game.getType().getNamedMoveQuery());		
		query.setParameter("game", game);
		/* HACK: Neeed to generalize this better */
		if (game.getType().equals(GameType.MANANTIALES)) {
			ManantialesGame manantiales = (ManantialesGame) game;
			query.setParameter("mode", manantiales.getMode());
		}
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
