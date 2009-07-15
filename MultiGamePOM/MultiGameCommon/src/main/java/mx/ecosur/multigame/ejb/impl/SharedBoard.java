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

package mx.ecosur.multigame.ejb.impl;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.enums.MoveStatus;

import mx.ecosur.multigame.model.ChatMessage;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Move;
import mx.ecosur.multigame.model.implementation.GameImpl;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SharedBoard implements SharedBoardLocal, SharedBoardRemote {
	
	private static Logger logger = Logger.getLogger(SharedBoard.class
			.getCanonicalName());
	
	@PersistenceContext (unitName="MultiGame")
	EntityManager em;
	
	public SharedBoard () throws InstantiationException, IllegalAccessException, 
		ClassNotFoundException 
	{
		super();
	}	
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardLocal#getGame(int)
	 */
	public Game getGame(int gameId) {
		logger.fine ("Getting game with id: " + gameId);
		
		/** TODO:  Inject or make this query static */
		Query query = em.createNamedQuery("getGameById");
		query.setParameter("id", gameId);
		GameImpl impl = null;
		try {
			impl = (GameImpl) query.getSingleResult();
		} catch (NoResultException e) {
			throw new RuntimeException ("UNABLE TO FIND GAME WITH ID: " + gameId);
		}
		
		return new Game (impl); 
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#move(mx.ecosur.multigame.model.Move)
	 */
	public Move move(Game game, Move move) throws InvalidMoveException {		
		logger.fine("Preparing to execute move " + move);
		
		/* Refresh a detached GamePlayer in the Move */
		GamePlayer player = move.getPlayer();
		
		/* Refresh a detached Game in GamePlayer */
		if (!em.contains (player.getGame().getImplementation())) {
			GameImpl impl = em.find (player.getGame().getImplementation().getClass(),
					player.getGame().getId());
			game = new Game (impl);
			player.setGame(game);			
		}
		
		if (!em.contains (player.getImplementation())) {
			player = new GamePlayer (em.find(
					player.getImplementation().getClass(), player.getId()));
		}		
		
		move.setPlayer(player);
		
		/* persist in order to define id */
		em.persist(move.getImplementation());

		/* Execute the move */
		move = game.move (move);		
		
		if (move.getStatus().equals(MoveStatus.INVALID)) 
			throw new InvalidMoveException ("INVALID Move.");

		return move;
		
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#getMoves(int)
	 */
	public Collection<Move> getMoves(int gameId) {
		Collection<Move> ret = null;
		
		Game game = getGame(gameId);
		if (game != null)
			ret = game.getMoves();
		
		return ret;
	}
	
	public void addMessage(ChatMessage chatMessage) {		
		/* chat message sender may be detatched */
		if (!em.contains(chatMessage.getSender())) {
			chatMessage.setSender(em.find(chatMessage.getSender().getClass(), 
					chatMessage.getSender().getId()));
		}

		em.persist(chatMessage);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.SharedBoardInterface#updateMove(mx.ecosur.multigame.model.Move)
	 */
	public Move updateMove(Move move) {
		em.merge(move.getImplementation());
		return move;
	}
}
