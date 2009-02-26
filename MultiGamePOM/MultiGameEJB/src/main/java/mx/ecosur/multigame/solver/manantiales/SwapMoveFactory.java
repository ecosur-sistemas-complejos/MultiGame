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

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.AbstractMoveFactory;
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

public class SwapMoveFactory extends AbstractMoveFactory {

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.factory.CachedMoveFactory#createCachedMoveList(org.drools.solver.core.solution.Solution)
	 */
	@SuppressWarnings("unchecked")
	public List<Move> createMoveList(Solution solution) {
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
		for (Color color : territoryMap.keySet()) {
			TreeSet<Token> territory = territoryMap.get(color);
			
			/* Score the territory */
			int score = 0;
			for (Token tok : territory) {
				switch (tok.getType()) {
				case SILVOPASTORAL:
					score += 4;
					break;
				case INTENSIVE_PASTURE:
					score += 3;
					break;
				case MODERATE_PASTURE:
					score += 2;
					break;
				case MANAGED_FOREST:
					score += 1;
					break;
				}
			}
		
			for (Token tok : territory) {
				/* All tokens can be swapped with another token in the 
				 * territory 
				 */
				for (Token swap : territory) {
					if (tok.equals(swap))
						continue;
					ret.add(new SwapMove (tok, swap));
				}
				
				/* Special case for Border tokens.
				 * 
				 *  We want to be able to swap all tokens 
				 *  on the border, including tokens from
				 *  different colored territories. 
				 * 
				 * */
				for (Color c : tok.getBorder().getColors()) {
					if (c.equals(color))
						continue;
					TreeSet<Token> borderTerritory = territoryMap.get(c);
					/* Null check for uncolored tokens */
					if (borderTerritory != null) {
						for (Token borderToken : borderTerritory) {
							if (borderToken.getBorder().equals(tok.getBorder())) {
								ret.add(new SwapMove (tok, borderToken));
							} else 
								continue;
						}
					}
				}
			}		
		}

		return ret;
	}
}
