/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Defines specific behavior of Checker objects, in a generically accessible 
 * manner.  Checker cells are either regular Checkers, moving forward or 
 * attacking by axis, or they have been "Kinged" and are able to move and attack
 * in all directions.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.checkers;

import mx.ecosur.multigame.Characteristic;

public class CheckerCharacteristic implements Characteristic {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5815949939979111669L;
	private boolean kinged;

	public CheckerCharacteristic () {
		super();
		this.kinged = false;
	}
	
	public CheckerCharacteristic (boolean kinged) {
		super();
		this.kinged = true;
	}

	public boolean isKinged() {
		return kinged;
	}

	public void setKinged(boolean kinged) {
		this.kinged = kinged;
	}
	
	public CheckerCharacteristic clone() {
		CheckerCharacteristic ret = new CheckerCharacteristic ();
		ret.setKinged(this.isKinged());
		return ret;
	}
	
	public String toString(){
		return "kinged = " + kinged;
	}
}
