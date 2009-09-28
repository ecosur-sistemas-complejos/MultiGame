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

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.model.GamePlayer;

public interface MoveImpl extends Implementation {
	
	/**
	 * @return
	 */
	public int getId();

	/**
	 * @return
	 */
	public MoveStatus getStatus();

	/**
	 * @return
	 */
	public GamePlayer getPlayerModel();

	/**
	 * @param player
	 */
	public void setPlayerModel(GamePlayer player);

	/**
	 * @return
	 */
	public CellImpl getCurrent();
	
	/**
	 * Sets the current cell. Each class that implements
	 * this method will cast the CellImpl to their preferred
	 * type of cell implementation.
	 * 
	 * @param cellImpl
	 */
	public void setCurrent(CellImpl cellImpl);

	/**
	 * @return
	 */
	public CellImpl getDestination();
	
	/**
	 * Sets the destination cell. Each class that implements
	 * this method will cast the CellImpl to their preferred
	 * type of cell implementation.
	 * 
	 * @param cellImpl
	 */
	public void setDestination(CellImpl cellImpl);

}
