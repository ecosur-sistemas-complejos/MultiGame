/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Describes an invalid registration.
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame.exception;


@SuppressWarnings("serial")
public class InvalidRegistrationException extends Exception {

	/**
	 * @param arg0
	 */
	public InvalidRegistrationException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InvalidRegistrationException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public InvalidRegistrationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
