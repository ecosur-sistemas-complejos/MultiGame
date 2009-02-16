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

import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.move.Move;

public class RowSwapMove implements Move {
	
	private Token token, swapToken;
	private int toRow;
	
	public RowSwapMove (Token token, Token swapToken, int toRow) {
		this.token = token;
		this.swapToken = swapToken;
		this.toRow = toRow;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#createUndoMove(org.drools.WorkingMemory)
	 */
	public Move createUndoMove(WorkingMemory workingMemory) {
		return new RowSwapMove (swapToken, token, token.getRow());
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#doMove(org.drools.WorkingMemory)
	 */
	public void doMove(WorkingMemory wm) {
		FactHandle tokenHandle = wm.getFactHandle(token);
		FactHandle swapHandle = wm.getFactHandle(swapToken);
		
		Token newToken = new Token (token.getColumn(), toRow, token.getColor(),
				token.getType());
		Token swappedToken = new Token (token.getColumn(), token.getRow(), token.getColor(),
				swapToken.getType());
		
		/* Update WorkingMemory in one block */
		synchronized (wm) {
			wm.modifyRetract(tokenHandle);
			wm.modifyInsert(tokenHandle, newToken);
			wm.modifyRetract (swapHandle);
			wm.modifyInsert (swapHandle, swappedToken);
		}	
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#isMoveDoable(org.drools.WorkingMemory)
	 */
	public boolean isMoveDoable(WorkingMemory wm) {
		return (token.getRow() != toRow);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.token + " => " + "(" + token.getColumn() + ", " + toRow + ")";
	}
}
