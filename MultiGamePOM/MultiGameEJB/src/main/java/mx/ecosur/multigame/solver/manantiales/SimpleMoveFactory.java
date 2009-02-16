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
 * The SimpleMoveFactory simply offers a set of all possible moves to 
 * the selector for evaluation, based upon each available token.
 * Hence, the factory drives through all tokens on the territory,
 * and returns a move of the differing token types to the rules engine
 * for scoring. 
 * 
 * With regards to Border areas, the SimpleMoveFactory returns a complement
 * of all possible move types for the border area.  This allows the solver
 * to swap out border areas with tokens from both bordering territories.
 * 
 * @author awaterma@ecosur.mx
 */
public class SimpleMoveFactory extends CachedMoveFactory {

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.factory.CachedMoveFactory#createCachedMoveList(org.drools.solver.core.solution.Solution)
	 */
	@Override
	public List<Move> createCachedMoveList(Solution solution) {		
		List<Move> ret = new ArrayList<Move> ();
		ret.addAll(calculateMoves((Set<Token>) solution.getFacts()));
		return ret;
	}
	
	/*
	 * Moves are calculated in a very simple fashion:
	 * 
	 * 1.  Get the current token.
	 * 2.  Suggest Moves for that same location based upon
	 * different token types.
	 * 3. Suggest complimentary moves for border regions.
	 * 
	 * The list is then scored and parsed by the solver.
	 */
	public List<Move> calculateMoves (Set<Token> tokens) {
		List<Move> ret = new ArrayList<Move> ();
		for (Token tok : tokens) {
			switch (tok.getType()) {
			case INTENSIVE_PASTURE:
				ret.add(new TokenMove(tok, getSuggestion(tok, 
						TokenType.MANAGED_FOREST)));
				ret.add(new TokenMove(tok, getSuggestion(tok, 
						TokenType.MODERATE_PASTURE)));
				break;
			case MANAGED_FOREST:
				ret.add(new TokenMove(tok, getSuggestion (tok, 
						TokenType.INTENSIVE_PASTURE)));
				ret.add(new TokenMove(tok, getSuggestion (tok, 
						TokenType.MODERATE_PASTURE)));
				break;
			case MODERATE_PASTURE:
				ret.add(new TokenMove(tok, getSuggestion(tok, 
						TokenType.MANAGED_FOREST)));
				ret.add(new TokenMove(tok, getSuggestion(tok, 
						TokenType.INTENSIVE_PASTURE)));
				break;
			case UNDEVELOPED:
					/* Special case */
				if (tok.getBorder().equals(BorderType.NONE)) {
					ret.add(new TokenMove(tok, getSuggestion(tok, 
							TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove(tok, getSuggestion (tok, 
							TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (tok, 
							TokenType.INTENSIVE_PASTURE)));
				}
					/* Undeveloped land on the border */
				else {
					switch (tok.getBorder()) {
					case NORTH:
						tok.setColor(Color.BLUE);
						ret.addAll(getBorderCompliment(tok));
						tok.setColor(Color.GREEN);
						ret.addAll(getBorderCompliment(tok));						
						break;
					case EAST:
						tok.setColor(Color.GREEN);
						ret.addAll(getBorderCompliment(tok));
						tok.setColor(Color.YELLOW);
						ret.addAll(getBorderCompliment(tok));													
						break;
					case SOUTH:
						tok.setColor(Color.RED);
						ret.addAll(getBorderCompliment(tok));
						tok.setColor(Color.YELLOW);
						ret.addAll(getBorderCompliment(tok));								
						break;
					case WEST:
						tok.setColor(Color.BLUE);
						ret.addAll(getBorderCompliment(tok));
						tok.setColor(Color.RED);
						ret.addAll(getBorderCompliment(tok));							
						break;					
					}	
				}	
				break;
			}
				/* Get all border suggestions if this is developed land
				 * and on the border 
				 */
			if (!tok.getType().equals(TokenType.UNDEVELOPED) && 
					!tok.getBorder().equals(BorderType.NONE))
					ret.addAll(getBorderCompliment(tok));
		}
		
		return ret;
	}
	
	private Token getSuggestion (Token tok, TokenType type) {
		return getSuggestion (tok, tok.getColor(), type);
	}

	private Token getSuggestion(Token tok, Color color, TokenType type) {
		return new Token (tok.getColumn(), tok.getRow(),
			color, type);
	}
	
	/* For a given developed piece of land on the border, get 
	 * the three types of moves for the bordering player
	 */
	private List<Move> getBorderCompliment (Token tok) {
		List<Move> ret = new ArrayList<Move>();
		switch(tok.getBorder()) {
			case NORTH:
				if (tok.getColor().equals(Color.GREEN)) {
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.BLUE, TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.BLUE, TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.BLUE, TokenType.INTENSIVE_PASTURE)));
				} else {
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.GREEN, TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.GREEN, TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.GREEN, TokenType.INTENSIVE_PASTURE)));
				}
				break;
			case EAST:
				if (tok.getColor().equals(Color.YELLOW)) {
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.GREEN, TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.GREEN, TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.GREEN, TokenType.INTENSIVE_PASTURE)));
				} else {
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.YELLOW, TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.YELLOW, TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.YELLOW, TokenType.INTENSIVE_PASTURE)));
				}
				break;
			case SOUTH:
				if (tok.getColor().equals(Color.YELLOW)) {
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.RED, TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove (tok, getSuggestion (
						tok, Color.RED, TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.RED, TokenType.INTENSIVE_PASTURE)));
				} else {
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.YELLOW, TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.YELLOW, TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.YELLOW, TokenType.INTENSIVE_PASTURE)));
				}
				break;
			case WEST:
				if (tok.getColor().equals(Color.RED)) {
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.BLUE, TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.BLUE, TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.BLUE, TokenType.INTENSIVE_PASTURE)));
				} else {
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.RED, TokenType.MANAGED_FOREST)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.RED, TokenType.MODERATE_PASTURE)));
					ret.add(new TokenMove(tok, getSuggestion (
						tok, Color.RED, TokenType.INTENSIVE_PASTURE)));
				}
				break;					
		}
		
		return ret;
	}
}