/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.ejb.entity.manantiales;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import mx.ecosur.multigame.impl.manantiales.Mode;
import mx.ecosur.multigame.impl.manantiales.TokenType;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Move;

@SuppressWarnings("serial")
@Entity
@NamedQueries( { 
	@NamedQuery(name = "getManantialesMoves", 
			query = "select mm from ManantialesMove mm " +
					"where mm.player.game=:game and mm.mode=:mode " +
					"order by mm.id asc") 
})
public class ManantialesMove extends Move {
	
	private TokenType type, replacementType;
	
	private boolean badYear, premium;
	
	private Mode mode;
	
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
		if (getDestination() == null) 
			type = TokenType.UNKNOWN;
		else {
			Ficha destination = (Ficha) getDestination();
			type = destination.getType();
		}
		
		return type;
	}
	
	public void setType (TokenType type) {
		this.type = type;
	}

	public TokenType getReplacementType() {
		if (replacementType == null) {
			replacementType = TokenType.UNKNOWN;			
			if (getCurrent() instanceof Ficha) {
					Ficha current = (Ficha) getCurrent();
					replacementType = current.getType();
			}
		}
		
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

	/**
	 * @return the mode
	 */
	@Enumerated(EnumType.STRING)
	public Mode getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}
}
