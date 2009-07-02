/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.interfaces;

import java.util.List;

import mx.ecosur.multigame.model.Model;

public interface RepositoryImpl {

	/**
	 * @param obj
	 * @return
	 */
	public boolean contains(Object obj);

	/**
	 * @param clazz
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object find(Class clazz, int id);

	/**
	 * @param obj
	 * @return
	 */
	public void persist(Object obj);

	/**
	 * @param obj
	 * @return
	 */
	public void refresh(Object obj);

	/**
	 * 
	 */
	public void flush();

	/**
	 * @param query
	 * @param settors 
	 * @return
	 */
	public List<Model> executeNamedQuery(String query, Object[] settors);

	/**
	 * @param object
	 */
	public void merge(Object object);

}
