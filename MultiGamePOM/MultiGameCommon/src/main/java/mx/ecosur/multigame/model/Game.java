/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;

@SuppressWarnings("serial")
public class Game implements Model, Serializable {
	
	private GameImpl gameImpl;
	
	public Game (GameImpl gameImpl) {
		this.gameImpl = gameImpl;
	}

	/**
	 * @return
	 */
	public int getId() {
		return gameImpl.getId();
	}

	/**
	 * @param registrant
	 * @return
	 */
	public GamePlayer registerPlayer(Registrant registrant) {
		return new GamePlayer(gameImpl.registerPlayer(registrant.getImplementation()));
	}

	/**
	 * @param player
	 */
	public void removePlayer(GamePlayer player) {
		gameImpl.removePlayer (player.getImplementation());		
	}

	/**
	 * @param end
	 */
	public void setState(GameState state) {
		gameImpl.setState(state);		
	}

	/**
	 * @return
	 */
	public GameState getState() {
		return gameImpl.getState();
	}

	/**
	 * 
	 */
	public void initialize() {
		gameImpl.initialize();
	}
	
	/**
	 * 
	 */
	public GamePlayer addPlayer (GamePlayer player) {
		return new GamePlayer (gameImpl.addPlayer(player.getImplementation()));
	}

	/**
	 * @param move
	 * @throws InvalidMoveException 
	 */
	public void move(Move move) throws InvalidMoveException {
		gameImpl.move(move.getImplementation());
	}

	/**
	 * @return
	 */
	public Collection<Move> getMoves() {
		Collection<Move> collection = new HashSet<Move>();
		Collection<MoveImpl> moves = gameImpl.getMoves();
		for (MoveImpl move : moves) {
			collection.add(new Move(move));
		}
		return collection;
	}

	/**
	 * @return
	 */
	public GameImpl getImplementation() {
		return gameImpl;
	}

}
