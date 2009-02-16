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
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.manantiales.Token;

import org.drools.solver.core.solution.Solution;

/**
 * The ManantialesSolution class is used for calculating the solution for the 
 * game of Manantiales
 * 
 * This class simply holds a handle to the game being examined by the rules
 * engine.  All state changes should be expressed in that Game object.
 */

public class ManantialesSolution implements Solution {
	
	private Set<Token> tokens;
	
	public ManantialesSolution (Set<Token> tokens) {
		this.tokens = tokens;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.solution.Solution#cloneSolution()
	 */
	public Solution cloneSolution() {
		Solution ret = null;
		
		try {
			Set<Token> clones = new TreeSet<Token>(new CellComparator());
			for (Token tok : tokens) {
				clones.add(tok.clone());
			}
			
			ret = new ManantialesSolution(clones);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.solution.Solution#getFacts()
	 */
	@SuppressWarnings("unchecked")
	public Collection<? extends Object> getFacts() {
		return tokens;
	}
	
	public void addToken (Token token) {
		tokens.remove(token);
		tokens.add(token);
	}
	
	public String getDistribution () {
		StringBuffer buf = new StringBuffer();
		for (Color c : Color.values()) {
			if (c.equals(Color.UNKNOWN))
				continue;
			buf.append (getDistribution(c) +"\n");
		}
		
		return buf.toString();
	}
	
	public String getDistribution(Color color) {
		int i = 0, m = 0, f = 0, s = 0;
		for (Token tok : tokens) {
			if (tok.getColor().equals(color)) {
				switch (tok.getType()) {
				case INTENSIVE_PASTURE:
					i++;
					break;
				case MODERATE_PASTURE:
					m++;
					break;
				case MANAGED_FOREST:
					f++;
					break;
				case SILVOPASTORAL:
					s++;
					break;	
				}
			}
		}
		
		return i + " I, " + m + " M, " + f + "F, " + s + " S.";
	}
}
