/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.solver.manantiales;

import java.util.Collection;
import java.util.Collections;

import mx.ecosur.multigame.ejb.entity.manantiales.Token;
import mx.ecosur.multigame.manantiales.TokenType;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.localsearch.decider.accepter.tabu.TabuPropertyEnabled;
import org.drools.solver.core.move.Move;

public class TokenMove implements Move, TabuPropertyEnabled {
	
	protected Token token;
	protected Token toToken;
	
	public TokenMove (Token token, Token toToken) {
		this.token = token;
		this.toToken = toToken;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#createUndoMove(org.drools.WorkingMemory)
	 */
	public Move createUndoMove(WorkingMemory wm) {
		return new TokenMove (toToken, token);
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#doMove(org.drools.WorkingMemory)
	 */
	public void doMove(WorkingMemory wm) {
		FactHandle tokenHandle = wm.getFactHandle(token);
		
		/* Update WorkingMemory */
		wm.modifyRetract(tokenHandle);
		wm.modifyInsert(tokenHandle, toToken);
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#isMoveDoable(org.drools.WorkingMemory)
	 */
	public boolean isMoveDoable(WorkingMemory wm) {		
		boolean ret = true;
		if (token.getColor().equals(toToken.getColor())) {
			if (token.getType().equals(toToken.getType())) {
				ret = token.getColumn() != toToken.getColumn() && 
					token.getRow() != toToken.getRow();
			} 
		} else if (token.getColor() != toToken.getColor())
			ret = toToken.getType().equals(TokenType.UNDEVELOPED);
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return token + "=>" + toToken;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.localsearch.decider.accepter.tabu.TabuPropertyEnabled#getTabuProperties()
	 */
	public Collection<? extends Object> getTabuProperties() {
		return Collections.singletonList(token);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof TokenMove) {
			TokenMove comparison = (TokenMove) obj;
				/* Moves are equal if it is the same change */
			if (comparison.toToken.equals(this.toToken))
				ret = true;
		} else {
			ret = super.equals(obj);
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder() 
        .append(token) 
        .append(toToken) 
        .toHashCode(); 
	}
}
