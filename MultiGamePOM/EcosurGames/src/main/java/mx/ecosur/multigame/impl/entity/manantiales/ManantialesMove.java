/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.manantiales;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;

import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridMove;

import mx.ecosur.multigame.model.implementation.GamePlayerImpl;

@Entity
@NamedQueries( { 
	@NamedQuery(name = "getManantialesMoves", 
			query = "select mm from ManantialesMove mm " +
					"where mm.player.game=:game and mm.mode=:mode " +
					"order by mm.id asc") 
})
public class ManantialesMove extends GridMove {
	
	private static final long serialVersionUID = 1L;

	private TokenType type, replacementType;
	
	private boolean badYear, premium;
	
	private Mode mode;
	
	public ManantialesMove () {
		super();
		badYear = false;
		premium = false;
	}
	
	public ManantialesMove (GridPlayer player, Ficha destination) {
		super (player, destination);
		type = destination.getType();
	}
	
	public ManantialesMove (GridPlayer player, Ficha current, Ficha destination)
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

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.MoveImpl#setPlayer(mx.ecosur.multigame.model.implementation.AgentImpl)
	 */
	public void setPlayer(GamePlayerImpl player) {
		this.player = (GridPlayer) player;
	}
}
