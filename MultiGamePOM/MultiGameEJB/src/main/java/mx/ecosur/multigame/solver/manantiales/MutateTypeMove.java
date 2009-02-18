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

import mx.ecosur.multigame.ejb.entity.manantiales.Token;
import mx.ecosur.multigame.manantiales.TokenType;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.move.Move;

public class MutateTypeMove implements Move {
	
	protected Token token;
	protected TokenType toType;
	
	public MutateTypeMove (Token token, TokenType toType) {
		this.token = token;
		this.toType = toType;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#createUndoMove(org.drools.WorkingMemory)
	 */
	public Move createUndoMove(WorkingMemory wm) {
		Token mutated = new Token (token.getColumn(), token.getRow(), token.getColor(),
				toType);
		return new MutateTypeMove (mutated, token.getType());
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#doMove(org.drools.WorkingMemory)
	 */
	public void doMove(WorkingMemory wm) {
		FactHandle tokenHandle = wm.getFactHandle(token);
		
		/* Update WorkingMemory */
		wm.modifyRetract(tokenHandle);
		token.setType(toType);
		wm.modifyInsert(tokenHandle, token);
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#isMoveDoable(org.drools.WorkingMemory)
	 */
	public boolean isMoveDoable(WorkingMemory wm) {		
		return ! (token.getType().equals(toType));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Token mutated = new Token (token.getColumn(), token.getRow(), 
				token.getColor(), toType);
		return token + "=>" + mutated;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof MutateTypeMove) {
			MutateTypeMove comparison = (MutateTypeMove) obj;
				/* Moves are equal if it is the same change */
			if (comparison.toType.equals(this.toType))
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
        .append(toType) 
        .toHashCode(); 
	}
}
