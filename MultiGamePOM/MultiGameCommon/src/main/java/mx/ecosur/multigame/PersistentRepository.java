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

import java.util.List;

import mx.ecosur.multigame.ejb.interfaces.RepositoryImpl;
import mx.ecosur.multigame.model.Model;

public class PersistentRepository {

	RepositoryImpl repositoryImpl;
	
	public PersistentRepository (RepositoryImpl repositoryImpl) {
		this.repositoryImpl = repositoryImpl;
	}

	/**
	 * @param game
	 * @return
	 */
	public boolean contains(Object obj) {
		return repositoryImpl.contains(obj);
	}

	/**
	 * @param class1
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object find(Class clazz, int id) {
		return repositoryImpl.find(clazz, id);
	}

	/**
	 * @param registrant
	 */
	public void persist(Object obj) {
		repositoryImpl.persist (obj);
	}

	/**
	 * @param game
	 */
	public void refresh(Object obj) {
		repositoryImpl.refresh (obj);
	}

	/**
	 * 
	 */
	public void flush() {
		repositoryImpl.flush ();
	}

	/**
	 * @param string
	 * @return
	 */
	public List <Model> executeNamedQuery(String query, Object...settors) {
		return repositoryImpl.executeNamedQuery (query, settors);
	}

	/**
	 * @param move
	 */
	public void merge(Object object) {
		repositoryImpl.merge(object);
	}
}
