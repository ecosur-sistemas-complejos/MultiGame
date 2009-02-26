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
import mx.ecosur.multigame.manantiales.BorderType;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.AbstractMoveFactory;
import org.drools.solver.core.solution.Solution;

public class RotateMoveFactory extends AbstractMoveFactory {
	
	Map<BorderType, TreeSet<Token>> borderMap;
	Set <Token> facts;
	
	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.factory.MoveFactory#createMoveList(org.drools.solver.core.solution.Solution)
	 */
	@SuppressWarnings("unchecked")
	public List<Move> createMoveList(Solution solution) {
		List<Move> ret = new ArrayList<Move>();
		
		/* Walk each piece and suggest all possible rotations on the borders */
		facts = (Set<Token>) solution.getFacts();
		borderMap = new HashMap<BorderType,TreeSet<Token>> ();
		
		/* Segregate border facts by location */
		for (Token token : facts) {
			/* Get all Tokens of this color from the set */
			if (borderMap.containsKey(token.getBorder()))
				continue;
			TreeSet<Token> border = new TreeSet<Token>(new CellComparator());
			for (Token internalTok : facts) {
				if (internalTok.getBorder().equals(token.getBorder()) 
						&& !internalTok.getBorder().equals(BorderType.NONE)) 
				{
					border.add (internalTok);
				}
			}
			borderMap.put (token.getBorder(), border);
		}
		
		/* Generate all rotation moves (Clockwise and Counterclockwise) from
		 * all colors
		 */
		for (BorderType border : BorderType.values()) {
			ret.addAll(fill (border, borderMap.get(border)));
		}
		
		return ret;
	}
	
	/*
	 * @TODO Refactor this fill method to remove redundant code.
	 */
	private List<Move> fill (BorderType border, Set<Token> tokens) {
		List<Move> ret = new ArrayList<Move>();
		switch (border) {
			case NORTH:
				for (Token token : tokens) {
					if (token.getColor().equals(Color.BLUE)) {
						Set<Token> seconds = borderMap.get(BorderType.WEST);
						for (Token second : seconds) {
							Set<Token> thirds = borderMap.get(BorderType.SOUTH);
							for (Token third : thirds) {
								Set<Token> fourths = borderMap.get(BorderType.EAST);
								for (Token fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.GREEN)) {
						Set<Token> seconds = borderMap.get(BorderType.EAST);
						for (Token second : seconds) {
							Set<Token> thirds = borderMap.get(BorderType.SOUTH);
							for (Token third : thirds) {
								Set<Token> fourths = borderMap.get(BorderType.WEST);
								for (Token fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					}
				}
				break;
			case EAST:
				for (Token token : tokens) {
					if (token.getColor().equals(Color.GREEN)) {
						Set<Token> seconds = borderMap.get(BorderType.NORTH);
						for (Token second : seconds) {
							Set<Token> thirds = borderMap.get(BorderType.WEST);
							for (Token third : thirds) {
								Set<Token> fourths = borderMap.get(BorderType.SOUTH);
									for (Token fourth : fourths) {
										ret.add(new RotateMove(token,second,third,fourth));
									}
								}
							}
						} else if (token.getColor().equals(Color.YELLOW)) {
							Set<Token> seconds = borderMap.get(BorderType.SOUTH);
							for (Token second : seconds) {
								Set<Token> thirds = borderMap.get(BorderType.WEST);
								for (Token third : thirds) {
									Set<Token> fourths = borderMap.get(BorderType.NORTH);
									for(Token fourth : fourths) {
										ret.add(new RotateMove(token,second,third,fourth));
									}
								}
							}
						}
					}
				break;
			case SOUTH:
				for (Token token : tokens) {
					if (token.getColor().equals(Color.RED)) {
						Set<Token> seconds = borderMap.get(BorderType.WEST);
						for (Token second : seconds) {
							Set<Token> thirds = borderMap.get(BorderType.NORTH);
							for (Token third : thirds) {
								Set<Token> fourths = borderMap.get(BorderType.EAST);
								for (Token fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.YELLOW)) {
						Set<Token> seconds = borderMap.get(BorderType.EAST);
						for (Token second : seconds) {
							Set<Token> thirds = borderMap.get(BorderType.NORTH);
							for (Token third : thirds) {
								Set<Token> fourths = borderMap.get(BorderType.WEST);
								for (Token fourth : fourths) {
									ret.add(new RotateMove(token, second, third, fourth));
								}
							}
						}
					}
				}
				break;
			case WEST:
				for (Token token : tokens) {
					if (token.getColor().equals(Color.RED)) {
						Set<Token> seconds = borderMap.get(BorderType.SOUTH);
						for (Token second : seconds) {
							Set<Token> thirds = borderMap.get(BorderType.EAST);
							for (Token third : thirds) {
								Set<Token> fourths = borderMap.get(BorderType.NORTH);
								for (Token fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.BLUE)) {
						Set<Token> seconds = borderMap.get(BorderType.NORTH);
						for (Token second : seconds) {
							Set<Token> thirds = borderMap.get(BorderType.EAST);
							for (Token third : thirds) {
								Set<Token> fourths = borderMap.get(BorderType.SOUTH);
								for (Token fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					}
				}
				break;
			default:
				break;
		}
		
		return ret;
	}
}
