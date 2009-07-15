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

import java.util.List;

import javax.persistence.EntityManager;


public interface RegistrantImpl extends Implementation {

	/**
	 * @return
	 */
	public int getId();

	/**
	 * @param currentTimeMillis
	 * @return
	 */
	public void setLastRegistration(long currentTimeMillis);

	/**
	 * @return
	 */
	public String getName();

	/**
	 * @param em 
	 * @return
	 */
	public List<GameImpl> getAvailableGames(EntityManager em);

	/**
	 * @return
	 */
	public List<GameImpl> getCurrentGames(EntityManager em);

}
