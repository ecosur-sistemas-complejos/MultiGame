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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.manantiales.Token;
import mx.ecosur.multigame.manantiales.TokenType;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.CachedMoveFactory;
import org.drools.solver.core.solution.Solution;

/**
 * The SwapMoveFactory generates a set of all possible swap moves by territory.
 * The SwapMoveFactory returns all possible Moves (Column and Row) for every
 * piece located on the board.  This results in a highly deterministic list of 
 * moves to be considered by the solver as it evaluates each step.  SwapMoves
 * differ from TokenMoves as SwapMoves swap the token from the territory to 
 * be moved TO with the location the token is moved FROM.  
 * 
 * This limits search to only token types of a given board configuration.  
 */

public class SwapMoveFactory extends CachedMoveFactory {

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.factory.CachedMoveFactory#createCachedMoveList(org.drools.solver.core.solution.Solution)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Move> createCachedMoveList(Solution solution) {
		List<Move> ret = new ArrayList<Move>();
		
		/* Walk each piece and suggest all possible moves within that territory */
		Set <Token> facts = (Set<Token>) solution.getFacts();
		Map<Color, TreeSet<Token>> territoryMap = new HashMap<Color,TreeSet<Token>> ();
		
		/* Segregate facts into territories */
		for (Token token : facts) {
			/* Get all Tokens of this color from the set */
			if (territoryMap.containsKey(token.getColor()))
				continue;
			TreeSet<Token> territory = new TreeSet<Token>(new CellComparator());
			for (Token internalTok : facts) {
				if (internalTok.getColor().equals(token.getColor())) {
					territory.add (internalTok);
				}
			}
			territoryMap.put (token.getColor(), territory);
		}	
			
		/* Setup suggestions based on all possible values per token per territory */
		for (Color color : Color.values()) {
			if (color.equals(Color.UNKNOWN))
				continue;
			TreeSet<Token> territory = territoryMap.get(color);
			for (Token tok : territory) {
					/* Moves are split into column and row moves */
					/* All moves in the column are added */
				for (int row = 0; row < 9; row++) {
					CellComparator comp = (CellComparator) territory.comparator();
					Token loc = new Token (tok.getColumn(), row, Color.UNKNOWN,
							TokenType.UNDEVELOPED);
					Set<Token> tail = territory.tailSet(loc);
					Token test = null;
					for (Token search : tail) {
						if (comp.compare(loc, search) == 0) {
							test = search;
							break;
						}
					}
					
					if (test != null)
						ret.add(new RowSwapMove (tok, test, row));
				}
				
				for (int column = 0; column < 9; column++) {
					CellComparator comp = (CellComparator) territory.comparator();
					Token loc = new Token (column, tok.getRow(), Color.UNKNOWN,
							TokenType.UNDEVELOPED);
					Set<Token> tail = territory.tailSet(loc);
					Token test = null;
					for (Token search : tail) {
						if (comp.compare(loc, search) == 0) {
							test = search;
							break;
						}
					}					
					
					if (test != null) 
						ret.add(new RowSwapMove (tok, test, column));
				}
			}		
		}		
		return ret;
	}
}
