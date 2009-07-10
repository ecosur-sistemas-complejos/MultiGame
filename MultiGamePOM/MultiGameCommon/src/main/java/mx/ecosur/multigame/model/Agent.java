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

import mx.ecosur.multigame.model.implementation.AgentImpl;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;


public class Agent extends GamePlayer {
	
	private static final long serialVersionUID = -6708647912533704063L;
	
	private AgentImpl implementation;
	
	
	public Agent (AgentImpl agentImpl) {
		super (agentImpl);
		this.implementation = agentImpl;
	}

	public void initialize() {
		implementation.initialize();
	}

	public Move determineNextMove() {
		MoveImpl move = implementation.determineNextMove();
		return new Move (move);
	}
	
	public Game findGame () {
		GameImpl gameImpl = implementation.findGame();
		return new Game(gameImpl);
	}
}
