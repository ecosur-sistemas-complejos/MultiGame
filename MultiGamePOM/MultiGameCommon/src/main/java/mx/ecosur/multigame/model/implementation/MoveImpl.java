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

public interface MoveImpl extends Implementation {

	/**
	 * @return
	 */
	public GamePlayerImpl getPlayer();

	/**
	 * @param gamePlayerImpl
	 */
	public void setPlayer(GamePlayerImpl gamePlayerImpl);

	/**
	 * @return
	 */
	public CellImpl getCurrent();

	/**
	 * @param cell
	 */
	public void setCurrent(CellImpl cell);

	/**
	 * @return
	 */
	public MoveStatus getStatus();

}
