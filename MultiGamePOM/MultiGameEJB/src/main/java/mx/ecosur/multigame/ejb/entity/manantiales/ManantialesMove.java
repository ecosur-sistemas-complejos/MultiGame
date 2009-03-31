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

import javax.persistence.Entity;

import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;

@SuppressWarnings("serial")
@Entity
public class ManantialesMove extends Move {
	
	public ManantialesMove () {
		super();
	}
	
	public ManantialesMove (GamePlayer player, Ficha destination) {
		super (player, destination);
	}
}
