/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
 */

package mx.ecosur.multigame.flexClient.exception;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import mx.ecosur.multigame.exception.InvalidMoveException;

import flex.messaging.MessageException;
import flex.messaging.util.ResourceLoader;

public class GameException extends MessageException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1656490171478522050L;
	public static final String EXCEPTION_TYPE_KEY = "EXCEPTION_TYPE";

	/**
	 * Default constructor
	 */
	public GameException() {
		super();
	}

	/**
	 * @param loader
	 */
	public GameException(ResourceLoader loader) {
		super(loader);
	}

	/**
	 * @param message
	 */
	public GameException(String message) {
		super(message);
	}

	/**
	 * @param t
	 */
	public GameException(Throwable t) {
		super(t);
	addExtendedData(t);
	}

	/**
	 * @param message
	 * @param t
	 */
	public GameException(String message, Throwable t,
			ExceptionType exceptionType) {
		super(message, t);
		addExtendedData(t);
	}

	private void addExtendedData(Throwable t) {
		ExceptionType et = ExceptionType.UNKNOWN_EXCEPTION;
		if (t.getClass().equals(InvalidMoveException.class)){
			et = ExceptionType.INVALID_MOVE;
		}else if (t.getClass().equals(RemoteException.class)){
			et = ExceptionType.REMOTE_EXCEPTION;
		}
		Map<String, ExceptionType> extendedData = new HashMap<String, ExceptionType>();
		extendedData.put(EXCEPTION_TYPE_KEY, et);
		setExtendedData(extendedData);
	}

}
