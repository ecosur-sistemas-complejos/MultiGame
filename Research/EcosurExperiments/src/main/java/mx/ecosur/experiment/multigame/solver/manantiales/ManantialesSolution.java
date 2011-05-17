/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.experiment.multigame.solver.manantiales;

import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;


import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.score.Score;

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
				ret = 32;
				break;
			}
			
			return ret;
		}
	}
	
	private SortedSet<SolverFicha> tokens;
	private Threshold umbra;
	private HashMap<Color, Distribution> distributionMap;
    private Score score;
	
	public ManantialesSolution (Threshold umbra, SortedSet<SolverFicha> tokens, Score score) {
		this (umbra);
		for (SolverFicha tok : tokens) {
			replaceToken(tok);
		}
        this.score = score;
	}
	
	public ManantialesSolution (Threshold umbra) {
		this.umbra = umbra;
		tokens = new TreeSet<SolverFicha> (new CellComparator());
        score = null;
		initialize();
	}	
	
	public void initialize() {
		for (int col = 0; col < 9; col++) {
			for (int row = 0; row < 9; row++) {
				if (row == 4 && col ==4)
					continue;
				Color color = null;
					/* All tokens across row 4 are set (except for the manantial) */
				if (row == 4 && col!=4) {
					if (col < 5) {
						color = Color.RED;
					} else
						color = Color.GREEN;
					tokens.add(new SolverFicha(col,row, color, TokenType.UNDEVELOPED));
					/* Cells are split by even/even and odd/odd (skip manantial) */
				} else if ( (row !=4 && col!=4) && ( 
						(col % 2 ==0 && row % 2 == 0) || (col % 2 !=0 && row % 2 !=0))) 
				{
					if (row < 4 && col < 5) 
						color = Color.BLUE;
					else if (row < 4 && col > 4)
						color = Color.GREEN;
					else if (row > 4 && col < 4)
						color = Color.RED;
					else if (row > 4 && col > 3)
						color = Color.YELLOW;
					tokens.add(new SolverFicha(col,row, color, TokenType.UNDEVELOPED));
				} else if (col == 4) {
					if (row < 5 ) 
						color = Color.BLUE;
					else if (row > 4)
						color = Color.YELLOW;
					tokens.add (new SolverFicha(col, row, color, TokenType.UNDEVELOPED));
				} else
					continue;
			}
		}
	}	

	/* (non-Javadoc)
	 * @see org.drools.planner.core.solution.Solution#cloneSolution()
	 */
	public Solution cloneSolution() {
		Solution ret = null;
		
		try {
			SortedSet<SolverFicha> clones = new TreeSet<SolverFicha>(
					new CellComparator());
			for (SolverFicha tok : tokens) {
				clones.add(tok.clone());
			}
			
			ret = new ManantialesSolution(umbra, clones, score);
            
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		return ret;
	}

	/**
	 * @return the distribution
	 */
	public Distribution getDistribution(Color color) {
		if (distributionMap == null) 
			populateDistributions();
		return distributionMap.get(color);
	}

	/**
	 * 
	 */
	private void populateDistributions() {
		for (Color color : Color.values()) {
			int forest = 0, moderate = 0, intensive = 0, silvopastoral = 0;
			
			if (color.equals(Color.UNKNOWN))
				continue;
			for (ManantialesFicha tok: tokens) {
				if (tok.getColor().equals(color)) {
					switch (tok.getType()) {
					case MANAGED_FOREST:
						forest++;
						break;
					case MODERATE_PASTURE:
						moderate++;
						break;
					case INTENSIVE_PASTURE:
						intensive++;
						break;
					case SILVOPASTORAL:
						silvopastoral++;
						break;
					default:
						break;
					}
				}
			}
			
			Distribution dist = new Distribution (color, forest, moderate,
					intensive, silvopastoral);
			addDistribution(dist);
		}
	}
	
	public SortedSet<ManantialesFicha> getUndevelopedBorders (Color color) {
		SortedSet<ManantialesFicha> ret = new TreeSet<ManantialesFicha>(new CellComparator());
		/* Add in all uncolored tokens */
		for (ManantialesFicha tok : tokens) {
			if (tok.getType().equals(TokenType.UNDEVELOPED)) {
				switch (tok.getBorder()) {
				case NORTH:
					if (color.equals(Color.BLUE) || color.equals(Color.GREEN))
						ret.add(tok);
					break;
				case EAST:
					if (color.equals(Color.GREEN) || color.equals(Color.YELLOW))
						ret.add(tok);
					break;
				case SOUTH:
					if (color.equals(Color.YELLOW) || color.equals(Color.RED))
						ret.add(tok);
					break;
				case WEST:
					if (color.equals(Color.RED) || color.equals(Color.BLUE))
						ret.add(tok);
					break;
				}
			}
		}
			
			return ret;
	}

	/**
	 * @param distribution the distribution to set
	 */
	public void addDistribution(Distribution distribution) {
		if (distributionMap == null) {
			distributionMap = new HashMap<Color, Distribution>();
		}
		
		distributionMap.put(distribution.color, distribution);
	}

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }/* (non-Javadoc)
	 * @see org.drools.planner.core.solution.Solution#getFacts()
	 */
	public Collection<? extends Object> getFacts() {
		return tokens;
	}
	
	
	public boolean replaceToken (SolverFicha token) {
		boolean ret = false;
		if (tokens.contains(token))
			ret = tokens.remove(token);
		tokens.add(token);
		return ret;
	}
	
	public Threshold getThreshold() {
		return umbra;
	}
	
	public void setThreshold(Threshold threshold) {
		umbra = threshold;
	}

	/**
	 * @return the tokens
	 */
	public SortedSet<SolverFicha> getTokens() {
		return tokens;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer ("\n");
		SortedSet<SolverFicha> subset;
		
		for (int row = 0; row < 9; row++) {
			boolean leadingTab = false;
			subset = null;
			if (row == 4) {
				subset = tokens.subSet(new SolverFicha(0,4, Color.UNKNOWN, TokenType.UNDEVELOPED),
						new SolverFicha(9,4,Color.UNKNOWN, TokenType.UNDEVELOPED));
			} else if (row % 2 == 0) {
				subset = tokens.subSet(new SolverFicha(0,row, Color.UNKNOWN, TokenType.UNDEVELOPED),
						new SolverFicha(9,row,Color.UNKNOWN, TokenType.UNDEVELOPED));
			} else {
				leadingTab = true;
				subset = tokens.subSet(new SolverFicha(1,row, Color.UNKNOWN, TokenType.UNDEVELOPED),
						new SolverFicha(9,row,Color.UNKNOWN, TokenType.UNDEVELOPED));
			}
			
			if (leadingTab)
				buf.append("  ");
			for (ManantialesFicha tok: subset) {
				if (tok.getColumn() == 4)
					buf.append("  ");
				else if (tok.getColumn() > 4 && row %2 == 0)
					buf.append(" ");
				switch (tok.getType()) {
				case INTENSIVE_PASTURE:
					buf.append ("I[" + tok.getColor() +"]");
					break;
				case MODERATE_PASTURE:
					buf.append ("M[" + tok.getColor() + "]");
					break;
				case MANAGED_FOREST:
					buf.append ("F[" + tok.getColor() + "]");
					break;
				case UNDEVELOPED:
					buf.append ("U[" + tok.getColor() + "]");
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
