/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import mx.ecosur.multigame.ejb.interfaces.RepositoryImpl;
import mx.ecosur.multigame.model.Model;

public class JPARepository implements RepositoryImpl {

	@PersistenceContext(unitName = "EcosurGames")
	private EntityManager em;
	

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RepositoryImpl#contains(java.lang.Object)
	 */
	public boolean contains(Object obj) {		
		return em.contains(obj);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RepositoryImpl#executeNamedQuery(java.lang.String, java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
	public List<Model> executeNamedQuery(String query, Object[] settors) {
		Query namedQuery = em.createNamedQuery(query);		
		int counter = 1;
		for (Object obj : settors) {
			namedQuery.setParameter(counter++, obj);
		}
		
		List result = namedQuery.getResultList();
		if (result != null)
			return (List<Model>) result;
		else
			return result;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RepositoryImpl#find(java.lang.Class, int)
	 */
	@SuppressWarnings("unchecked")
	public Object find(Class clazz, int id) {
		return em.find(clazz, id);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RepositoryImpl#flush()
	 */
	public void flush() {
		em.flush();
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RepositoryImpl#merge(java.lang.Object)
	 */
	public void merge(Object object) {
		em.merge(object);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RepositoryImpl#persist(java.lang.Object)
	 */
	public void persist(Object obj) {
		em.persist(obj);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RepositoryImpl#refresh(java.lang.Object)
	 */
	public void refresh(Object obj) {
		em.refresh(obj);
	}

}
