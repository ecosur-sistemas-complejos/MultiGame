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
import java.util.SortedSet;
import java.util.TreeSet;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.manantiales.Token;
import mx.ecosur.multigame.manantiales.TokenType;

import org.drools.solver.core.solution.Solution;

/**
 * The ManantialesSolution class is used for calculating the solution for the 
 * game of Manantiales
 * 
 * This class simply holds a handle to the game being examined by the rules
 * engine.  All state changes should be expressed in that Game object.
 */

public class ManantialesSolution implements Solution {
	
	public enum Threshold {
		SIMPLE, INNOVATIVE;
		
		public int value () {
			int ret = 0;
			switch (this) {
			case SIMPLE:
				ret = 24;
				break;
			case INNOVATIVE:
				ret = 28;
				break;
			}
			
			return ret;
		}
	}
	
	private SortedSet<Token> tokens;
	private Threshold umbra;
	
	
	public ManantialesSolution (Threshold umbra, SortedSet<Token> tokens) {
		this.umbra = umbra;
		this.tokens = tokens;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.solution.Solution#cloneSolution()
	 */
	public Solution cloneSolution() {
		Solution ret = null;
		
		try {
			SortedSet<Token> clones = new TreeSet<Token>(new CellComparator());
			for (Token tok : tokens) {
				clones.add(tok.clone());
			}
			
			ret = new ManantialesSolution(umbra, clones);
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
	
	
	public boolean replaceToken (Token token) {
		boolean ret = tokens.remove(token);
		tokens.add(token);
		return ret;
	}
	
	public Threshold getThreshold() {
		return umbra;
	}
	
	public void setThreshold(Threshold threshold) {
		umbra = threshold;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer ("\n");
		SortedSet<Token> subset;
		
		for (int row = 0; row < 9; row++) {
			boolean leadingTab = false;
			subset = null;
			if (row == 4) {
				subset = tokens.subSet(new Token (0,4, Color.UNKNOWN, TokenType.UNDEVELOPED),
						new Token (9,4,Color.UNKNOWN, TokenType.UNDEVELOPED));
			} else if (row % 2 == 0) {
				subset = tokens.subSet(new Token (0,row, Color.UNKNOWN, TokenType.UNDEVELOPED),
						new Token (9,row,Color.UNKNOWN, TokenType.UNDEVELOPED));				
			} else {
				leadingTab = true;
				subset = tokens.subSet(new Token (1,row, Color.UNKNOWN, TokenType.UNDEVELOPED),
						new Token (9,row,Color.UNKNOWN, TokenType.UNDEVELOPED));
			}
			
			if (leadingTab)
				buf.append("  ");
			for (Token tok: subset) {
				if (tok.getColumn() == 4)
					buf.append("  ");
				else if (tok.getColumn() > 4 && row %2 == 0)
					buf.append(" ");
				switch (tok.getType()) {
				case INTENSIVE_PASTURE:
					buf.append ("I");
					break;
				case MODERATE_PASTURE:
					buf.append ("M");
					break;
				case MANAGED_FOREST:
					buf.append ("F");
					break;
				case UNDEVELOPED:
					buf.append ("U");
				}
				if (row == 4)
					buf.append(" ");
				else if (tok.getColumn() != 3)
					buf.append ("  ");
			}
			buf.append ("\n");	
		}
		return buf.toString();
	}
}
