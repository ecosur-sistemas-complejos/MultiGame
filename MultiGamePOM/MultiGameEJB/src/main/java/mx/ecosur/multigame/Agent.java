/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame;

import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.Move;

public interface Agent {

	/* Returns the next move in the game as determined by the Agent's 
	 * implementation.
	 */
	public Move determineNextMove (Game game);
	
	/*
	 * Returns this agent's type.
	 */
	public Type getType ();	
	
}
