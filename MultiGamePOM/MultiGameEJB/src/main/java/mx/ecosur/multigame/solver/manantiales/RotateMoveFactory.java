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
import mx.ecosur.multigame.ejb.entity.manantiales.Ficha;
import mx.ecosur.multigame.manantiales.BorderType;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.AbstractMoveFactory;
import org.drools.solver.core.solution.Solution;

public class RotateMoveFactory extends AbstractMoveFactory {
	
	Map<BorderType, TreeSet<Ficha>> borderMap;
	Set <Ficha> facts;
	
	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.factory.MoveFactory#createMoveList(org.drools.solver.core.solution.Solution)
	 */
	@SuppressWarnings("unchecked")
	public List<Move> createMoveList(Solution solution) {
		List<Move> ret = new ArrayList<Move>();
		
		/* Walk each piece and suggest all possible rotations on the borders */
		facts = (Set<Ficha>) solution.getFacts();
		borderMap = new HashMap<BorderType,TreeSet<Ficha>> ();
		
		/* Segregate border facts by location */
		for (Ficha token : facts) {
			/* Get all Tokens of this color from the set */
			if (borderMap.containsKey(token.getBorder()))
				continue;
			TreeSet<Ficha> border = new TreeSet<Ficha>(new CellComparator());
			for (Ficha internalTok : facts) {
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
	private List<Move> fill (BorderType border, Set<Ficha> tokens) {
		List<Move> ret = new ArrayList<Move>();
		switch (border) {
			case NORTH:
				for (Ficha token : tokens) {
					if (token.getColor().equals(Color.BLUE)) {
						Set<Ficha> seconds = borderMap.get(BorderType.WEST);
						for (Ficha second : seconds) {
							Set<Ficha> thirds = borderMap.get(BorderType.SOUTH);
							for (Ficha third : thirds) {
								Set<Ficha> fourths = borderMap.get(BorderType.EAST);
								for (Ficha fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.GREEN)) {
						Set<Ficha> seconds = borderMap.get(BorderType.EAST);
						for (Ficha second : seconds) {
							Set<Ficha> thirds = borderMap.get(BorderType.SOUTH);
							for (Ficha third : thirds) {
								Set<Ficha> fourths = borderMap.get(BorderType.WEST);
								for (Ficha fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					}
				}
				break;
			case EAST:
				for (Ficha token : tokens) {
					if (token.getColor().equals(Color.GREEN)) {
						Set<Ficha> seconds = borderMap.get(BorderType.NORTH);
						for (Ficha second : seconds) {
							Set<Ficha> thirds = borderMap.get(BorderType.WEST);
							for (Ficha third : thirds) {
								Set<Ficha> fourths = borderMap.get(BorderType.SOUTH);
									for (Ficha fourth : fourths) {
										ret.add(new RotateMove(token,second,third,fourth));
									}
								}
							}
						} else if (token.getColor().equals(Color.YELLOW)) {
							Set<Ficha> seconds = borderMap.get(BorderType.SOUTH);
							for (Ficha second : seconds) {
								Set<Ficha> thirds = borderMap.get(BorderType.WEST);
								for (Ficha third : thirds) {
									Set<Ficha> fourths = borderMap.get(BorderType.NORTH);
									for(Ficha fourth : fourths) {
										ret.add(new RotateMove(token,second,third,fourth));
									}
								}
							}
						}
					}
				break;
			case SOUTH:
				for (Ficha token : tokens) {
					if (token.getColor().equals(Color.RED)) {
						Set<Ficha> seconds = borderMap.get(BorderType.WEST);
						for (Ficha second : seconds) {
							Set<Ficha> thirds = borderMap.get(BorderType.NORTH);
							for (Ficha third : thirds) {
								Set<Ficha> fourths = borderMap.get(BorderType.EAST);
								for (Ficha fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.YELLOW)) {
						Set<Ficha> seconds = borderMap.get(BorderType.EAST);
						for (Ficha second : seconds) {
							Set<Ficha> thirds = borderMap.get(BorderType.NORTH);
							for (Ficha third : thirds) {
								Set<Ficha> fourths = borderMap.get(BorderType.WEST);
								for (Ficha fourth : fourths) {
									ret.add(new RotateMove(token, second, third, fourth));
								}
							}
						}
					}
				}
				break;
			case WEST:
				for (Ficha token : tokens) {
					if (token.getColor().equals(Color.RED)) {
						Set<Ficha> seconds = borderMap.get(BorderType.SOUTH);
						for (Ficha second : seconds) {
							Set<Ficha> thirds = borderMap.get(BorderType.EAST);
							for (Ficha third : thirds) {
								Set<Ficha> fourths = borderMap.get(BorderType.NORTH);
								for (Ficha fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.BLUE)) {
						Set<Ficha> seconds = borderMap.get(BorderType.NORTH);
						for (Ficha second : seconds) {
							Set<Ficha> thirds = borderMap.get(BorderType.EAST);
							for (Ficha third : thirds) {
								Set<Ficha> fourths = borderMap.get(BorderType.SOUTH);
								for (Ficha fourth : fourths) {
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
