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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.grid.CellComparator;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.AbstractMoveFactory;
import org.drools.planner.core.solution.Solution;

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
	 * @see org.drools.planner.core.move.factory.CachedMoveFactory#createCachedMoveList(org.drools.planner.core.solution.Solution)
	 */
	@SuppressWarnings("unchecked")
	public List<Move> createMoveList(Solution solution) {
		List<Move> ret = new ArrayList<Move>();
		
		/* Walk each piece and suggest all possible moves within that territory */
		Set <ManantialesFicha> facts = (Set<ManantialesFicha>) solution.getFacts();
		Map<Color, TreeSet<ManantialesFicha>> territoryMap = new HashMap<Color,TreeSet<ManantialesFicha>> ();
		
		/* Segregate facts into territories */
		for (ManantialesFicha token : facts) {
			/* Get all Tokens of this color from the set */
			if (territoryMap.containsKey(token.getColor()))
				continue;
			TreeSet<ManantialesFicha> territory = new TreeSet<ManantialesFicha>(new CellComparator());
			for (ManantialesFicha internalTok : facts) {
				if (internalTok.getColor().equals(token.getColor())) {
					territory.add (internalTok);
				}
			}
			territoryMap.put (token.getColor(), territory);
		}
			
		/* Setup suggestions based on all possible values per token per territory */
		for (Color color : territoryMap.keySet()) {
			TreeSet<ManantialesFicha> territory = territoryMap.get(color);
			
			/* Score the territory */
			int score = 0;
			for (ManantialesFicha tok : territory) {
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
		
			for (ManantialesFicha tok : territory) {
				/* All tokens can be swapped with another token in the 
				 * territory 
				 */
				for (ManantialesFicha swap : territory) {
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
					TreeSet<ManantialesFicha> borderTerritory = territoryMap.get(c);
					/* Null check for uncolored tokens */
					if (borderTerritory != null) {
						for (ManantialesFicha borderToken : borderTerritory) {
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
