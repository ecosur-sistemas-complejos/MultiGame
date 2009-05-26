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
	
	private boolean badYear, premium;
	
	public ManantialesMove () {
		super();
		badYear = false;
		premium = false;
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
		if (type == null && destination != null)
			type = ( (Ficha) destination).getType();
		else if (type == null && destination == null)
			type = TokenType.UNKNOWN;
		return type;
	}
	
	public void setType (TokenType type) {
		this.type = type;
	}

	public TokenType getReplacementType() {
		if (replacementType == null && current != null)
			replacementType = ( (Ficha) current).getType();
		else if (replacementType == null)
			replacementType = TokenType.UNKNOWN;
		return replacementType;
	}

	public void setReplacementType(TokenType replacementType) {
		this.replacementType = replacementType;
	}
	
	public boolean isBadYear () {
		return badYear;
	}
	
	public void setBadYear (boolean year) {
		badYear = year;
	}
	
	public boolean isPremium () {
		return premium;
	}
	
	public void setPremium (boolean premium) {
		this.premium = premium;
	}
}
