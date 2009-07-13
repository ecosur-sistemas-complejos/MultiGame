/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model.implementation;

public interface GamePlayerImpl extends Implementation {
	
	public int getId();	
	
	public boolean isTurn();

	public void setTurn(boolean bool);

	public GameImpl getGame();

	public void setGame(GameImpl implementation);

}
