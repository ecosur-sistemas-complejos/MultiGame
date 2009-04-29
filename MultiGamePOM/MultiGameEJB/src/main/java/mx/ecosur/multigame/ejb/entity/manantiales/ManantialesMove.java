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
import mx.ecosur.multigame.manantiales.TokenType;

@SuppressWarnings("serial")
@Entity
public class ManantialesMove extends Move {
	
	private TokenType type, replacementType;
	
	public ManantialesMove () {
		super();
	}
	
	public ManantialesMove (GamePlayer player, Ficha destination) {
		super (player, destination);
		type = destination.getType();
	}
	
	public ManantialesMove (GamePlayer player, Ficha current, Ficha destination)
	{
		super (player, current, destination);
	}

	public TokenType getType () {
		if (type == null)
			type = ( (Ficha) getDestination()).getType();
		return type;
	}
	
	public void setType (TokenType type) {
		this.type = type;
	}

	public TokenType getReplacementType() {
		return replacementType;
	}

	public void setReplacementType(TokenType replacementType) {
		this.replacementType = replacementType;
	}
}
