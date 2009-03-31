/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.manantiales;

import mx.ecosur.multigame.ejb.entity.GamePlayer;

public class CheckConstraint {
	
	String reason;
	GamePlayer initiator;
	Object [] violators;
	
	public CheckConstraint () {
		super();
	}
	
	public CheckConstraint (String reason, GamePlayer initiator, Object...violators) 
	{
		super();
		this.reason = reason;
		this.initiator = initiator;
		this.violators = violators;
	}
	
	public CheckConstraint (String reason, Object...reasons) {
		super();
		this.reason = reason;
		this.violators = reasons;
	}

}
