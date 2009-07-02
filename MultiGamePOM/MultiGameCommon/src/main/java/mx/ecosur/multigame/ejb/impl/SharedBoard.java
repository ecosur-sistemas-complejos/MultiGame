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

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import mx.ecosur.multigame.PersistentRepository;

import mx.ecosur.multigame.ejb.interfaces.RepositoryImpl;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.enums.MoveStatus;

import mx.ecosur.multigame.model.Cell;
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
	
	PersistentRepository pr;
	
	@Resource
	String repositoryImpl;
	
	public SharedBoard () throws InstantiationException, IllegalAccessException, 
		ClassNotFoundException 
	{
		RepositoryImpl impl = (RepositoryImpl) Class.forName(repositoryImpl).newInstance();
		pr = new PersistentRepository (impl);		
	}	
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardLocal#getGame(int)
	 */
	public Game getGame(int gameId) {
		logger.fine ("Getting game with id: " + gameId);
		GameImpl impl = (GameImpl) pr.find (GameImpl.class, gameId);
		if (impl == null)
			throw new RuntimeException ("UNABLE TO FIND GAME WITH ID: " + gameId);
		return new Game(impl);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.SharedBoardRemote#move(mx.ecosur.multigame.model.Move)
	 */
	public Move move(Move move) throws InvalidMoveException {		
		logger.fine("Preparing to execute move " + move);

		/* Obtain attached instance of game and player */
		GamePlayer player = (GamePlayer) move.getPlayer();
		if (!pr.contains(player.getImplementation()))
			player = (GamePlayer) pr.find(player.getImplementation().getClass(), 
					player.getId());

		/* Refresh the move with the managed game and player */
		move.setPlayer(player);

		/* persist in order to define id */
		Cell current = move.getCurrent();
		if (current != null)
			move.setCurrent ((Cell) pr.find (current.getImplementation().getClass(), 
					current.getId()));
		pr.persist(move.getImplementation());		
		
		Game game = player.getGame();

		/* Execute the move in the rules */
		game.move (move);
		
			/* Merge all changes */
		pr.merge(move.getImplementation());		
		pr.flush();
		
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
		if (!pr.contains(chatMessage.getSender())) {
			chatMessage.setSender((GamePlayer) pr.find(GamePlayer.class, chatMessage.getSender()
					.getId()));
		}

		pr.persist(chatMessage);
		pr.flush();
	}
}
