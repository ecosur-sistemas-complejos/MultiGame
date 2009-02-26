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
import java.util.List;
import java.util.Set;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.manantiales.Token;
import mx.ecosur.multigame.manantiales.BorderType;
import mx.ecosur.multigame.manantiales.TokenType;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.CachedMoveFactory;
import org.drools.solver.core.solution.Solution;

/**
 * The MutateMoveFactory simply offers a set of all possible token mutations
 * (based on type) to the selector for evaluation.
 * 
 * With regards to Border areas, the MutateMoveFactory returns a set of color
 * mutations that are applicable to the specific border area that a token
 * is placed.
 * 
 * @author awaterma@ecosur.mx
 */
public class MutateMoveFactory extends CachedMoveFactory {

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.factory.CachedMoveFactory#createCachedMoveList(org.drools.solver.core.solution.Solution)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Move> createCachedMoveList(Solution solution) {		
		List<Move> ret = new ArrayList<Move> ();
		ret.addAll(calculateMoves((Set<Token>) solution.getFacts()));
		return ret;
	}

	
	public List<Move> calculateMoves (Set<Token> tokens) {
		List<Move> ret = new ArrayList<Move> ();
		for (Token tok : tokens) {
			switch (tok.getType()) {
			case INTENSIVE_PASTURE:
				ret.add(new MutateTypeMove(tok, TokenType.MANAGED_FOREST));
				ret.add(new MutateTypeMove(tok,TokenType.MODERATE_PASTURE));
				break;
			case MANAGED_FOREST:
				ret.add(new MutateTypeMove(tok, TokenType.INTENSIVE_PASTURE));
				ret.add(new MutateTypeMove(tok,TokenType.MODERATE_PASTURE));
				break;
			case MODERATE_PASTURE:
				ret.add(new MutateTypeMove(tok, TokenType.MANAGED_FOREST));
				ret.add(new MutateTypeMove(tok,TokenType.INTENSIVE_PASTURE));
				break;
			case UNDEVELOPED:
					ret.add(new MutateTypeMove(tok, TokenType.MANAGED_FOREST));
					ret.add(new MutateTypeMove(tok, TokenType.MODERATE_PASTURE));
					ret.add(new MutateTypeMove(tok, TokenType.INTENSIVE_PASTURE));
				}
			
			/* For tokens on the border, we offer a color mutator */
			if (tok.getBorder().equals(BorderType.NONE))
				continue;
			
			switch (tok.getBorder()) {
			case NORTH:
				if (tok.getColor().equals(Color.BLUE))
					ret.add(new MutateColorMove(tok, Color.GREEN));
				else
					ret.add(new MutateColorMove(tok, Color.BLUE));
				break;
			case EAST:
				if (tok.getColor().equals(Color.GREEN))
					ret.add(new MutateColorMove(tok, Color.YELLOW));
				else
					ret.add(new MutateColorMove(tok, Color.GREEN));
				break;
			case SOUTH:
				if (tok.getColor().equals(Color.RED))
					ret.add(new MutateColorMove(tok, Color.YELLOW));
				else
					ret.add(new MutateColorMove(tok, Color.RED));
				break;
			case WEST:
				if (tok.getColor().equals(Color.RED))
					ret.add(new MutateColorMove(tok, Color.BLUE));
				else
					ret.add(new MutateColorMove(tok, Color.RED));
				break;				
			}
		}
		
		return ret;
	}
}