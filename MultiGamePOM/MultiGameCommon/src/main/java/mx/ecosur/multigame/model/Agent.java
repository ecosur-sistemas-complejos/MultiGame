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
import mx.ecosur.multigame.model.implementation.MoveImpl;


public class Agent extends GamePlayer {
	
	private AgentImpl implementation;
	
	
	public Agent (AgentImpl agentImpl) {
		super (agentImpl);
		this.implementation = agentImpl;
	}

	public void initialize() {
		implementation.initialize();
	}

	public Move nextMove() {
		MoveImpl move = implementation.nextMove();
		return new Move (move);
	}
}
