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

import mx.ecosur.multigame.model.implementation.RegistrantImpl;

public class Registrant implements Model {
	
	private RegistrantImpl playerImpl;

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
}
