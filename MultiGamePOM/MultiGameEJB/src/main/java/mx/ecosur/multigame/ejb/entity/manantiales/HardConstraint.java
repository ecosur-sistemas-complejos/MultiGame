/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.entity.manantiales;

public class HardConstraint {
	
	String reason;
	Object [] violators;
	
	public HardConstraint() {
		super();
	}
	
	public HardConstraint (String reason, Object... violators) {
		super();
		this.reason = reason;
		this.violators = violators;
	}
}
