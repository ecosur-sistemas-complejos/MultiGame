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

import java.io.Serializable;

import mx.ecosur.multigame.model.implementation.Implementation;

public interface Model extends Serializable {
	
	public void setImplementation (Implementation impl);
	
	public Implementation getImplementation();
}
