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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.Implementation;
import mx.ecosur.multigame.model.implementation.RegistrantImpl;

public class Registrant implements Model {

	private static final long serialVersionUID = 1672849851772628554L;
	
	private RegistrantImpl playerImpl;
	
	public Registrant() {
		super();
	}

	public Registrant (RegistrantImpl playerImpl) {
		this.playerImpl = playerImpl;
	}

	/**
	 * @return
	 */
	public int getId() {
		return playerImpl.getId();
	}

	/**
	 * @param currentTimeMillis
	 */
	public void setLastRegistration(long currentTimeMillis) {
		playerImpl.setLastRegistration(currentTimeMillis);		
	}
	
	
	public RegistrantImpl getImplementation() {
		return playerImpl;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Model#setImplementation(mx.ecosur.multigame.model.implementation.Implementation)
	 */
	public void setImplementation(Implementation impl) {
		this.playerImpl = (RegistrantImpl) impl;
	}	
}
