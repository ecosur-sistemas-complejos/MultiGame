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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;

public class Game implements Model {

	private static final long serialVersionUID = 4651502618321513050L;
	
	private GameImpl gameImpl;
	
	public Game() {
		super();
	}
	
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
	 * @param agent
	 * @return Agent
	 */
	public Agent registerAgent (Agent agent) {
		return new Agent (gameImpl.registerAgent (agent.getImplementation()));
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
	 * @param move
	 * @throws InvalidMoveException 
	 */
	public Move move(Move move) throws InvalidMoveException {
		return new Move (gameImpl.move(move.getImplementation()));
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
	 * 
	 */
	public int getMaxPlayers () {
		return gameImpl.getMaxPlayers();
	}
	
	/**
	 * 
	 */
	public List<GamePlayer> listPlayers () {
		return gameImpl.listPlayers();
	}

	/**
	 * @return
	 */
	public GameImpl getImplementation() {
		return gameImpl;
	}
	
	public void setImplementation(GameImpl gameImpl) {
		this.gameImpl = gameImpl;
	}
}
