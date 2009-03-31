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

import mx.ecosur.multigame.ejb.entity.manantiales.Ficha;
import mx.ecosur.multigame.manantiales.TokenType;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.move.Move;

/*
 * Swaps two tokens of the same color.  
 */
public class SwapMove implements Move {
	
	protected Ficha token, swapToken;
	
	public SwapMove (Ficha token, Ficha swapToken) {
		this.token = token;
		this.swapToken = swapToken;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#createUndoMove(org.drools.WorkingMemory)
	 */
	public Move createUndoMove(WorkingMemory workingMemory) {
		return new SwapMove (swapToken, token);
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#doMove(org.drools.WorkingMemory)
	 */
	public void doMove(WorkingMemory wm) {
		int tokenColumn, tokenRow;
		int swapColumn, swapRow;
		
		tokenColumn = token.getColumn();
		tokenRow = token.getRow();
		
		swapColumn = swapToken.getColumn();
		swapRow = swapToken.getRow();
		
		FactHandle tokenHandle = wm.getFactHandle(token);
		FactHandle swapHandle = wm.getFactHandle(swapToken);
		
		wm.modifyRetract(tokenHandle);
		token.setColumn(swapColumn);
		token.setRow (swapRow);
		wm.modifyInsert(tokenHandle, token);
		wm.modifyRetract (swapHandle);
		swapToken.setColumn (tokenColumn);
		swapToken.setRow(tokenRow);
		wm.modifyInsert (swapHandle, token);	
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#isMoveDoable(org.drools.WorkingMemory)
	 */
	public boolean isMoveDoable(WorkingMemory wm) {
		return (!token.equals(swapToken)  && 
				!swapToken.getType().equals(TokenType.UNDEVELOPED));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "swap " + this.token + " => " + swapToken;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof SwapMove) {
			SwapMove comparison = (SwapMove) obj;
				/* Moves are equal if it is the same change */
			if (comparison.token.equals(this.token))
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
        .append(swapToken) 
        .toHashCode(); 
	}
}
