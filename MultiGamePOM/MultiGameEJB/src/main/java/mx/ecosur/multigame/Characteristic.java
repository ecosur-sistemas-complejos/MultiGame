/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Characteristics are specific to the game being modeled.  Each cell has
 * a "characteristic" field which allows implementors to express different
 * modes for specific games.  For example, Checkers has a characteristic that
 * defines a piece as being regular or a "King".
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame;

import java.io.Serializable;


public interface Characteristic extends Serializable, Cloneable {
	
	public Characteristic clone() throws CloneNotSupportedException;

}
