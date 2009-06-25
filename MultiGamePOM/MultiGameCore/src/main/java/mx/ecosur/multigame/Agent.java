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

import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.Player;

public interface Agent {

	/**
	 * @param game
	 * @param registrant
	 * @param favoriteColor
	 */
	public void initialize(Game game, Player registrant, Color favoriteColor);

	/**
	 * @return
	 */
	public Color getColor();

	/**
	 * @param color
	 */
	public void setColor(Color color);
	
	public boolean isTurn ();
	
	public void setTurn (boolean bool);

	/**
	 * @return
	 */
	public Game getGame();
	
	/**
	 * @void
	 */
	public void setGame (Game game);

	/**
	 * @return
	 */
	public Player getPlayer();
	
}
