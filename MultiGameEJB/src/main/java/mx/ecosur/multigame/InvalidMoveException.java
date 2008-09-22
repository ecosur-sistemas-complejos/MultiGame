/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * 
 * The InvalidMoveException describes the problem with a specific move
 * against a shared board.
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame;


@SuppressWarnings("serial")
public class InvalidMoveException extends Exception {

	public InvalidMoveException(String message) {
		super (message);
	}

}
