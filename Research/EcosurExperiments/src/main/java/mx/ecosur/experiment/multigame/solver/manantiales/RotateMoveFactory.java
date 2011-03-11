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
import mx.ecosur.multigame.impl.enums.manantiales.BorderType;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.AbstractMoveFactory;
import org.drools.planner.core.solution.Solution;

public class RotateMoveFactory extends AbstractMoveFactory {
	
	Map<BorderType, TreeSet<ManantialesFicha>> borderMap;
	Set <ManantialesFicha> facts;
	
	/* (non-Javadoc)
	 * @see org.drools.planner.core.move.factory.MoveFactory#createMoveList(org.drools.planner.core.solution.Solution)
	 */
	@SuppressWarnings("unchecked")
	public List<Move> createMoveList(Solution solution) {
		List<Move> ret = new ArrayList<Move>();
		
		/* Walk each piece and suggest all possible rotations on the borders */
		facts = (Set<ManantialesFicha>) solution.getFacts();
		borderMap = new HashMap<BorderType,TreeSet<ManantialesFicha>> ();
		
		/* Segregate border facts by location */
		for (ManantialesFicha token : facts) {
			/* Get all Tokens of this color from the set */
			if (borderMap.containsKey(token.getBorder()))
				continue;
			TreeSet<ManantialesFicha> border = new TreeSet<ManantialesFicha>(new CellComparator());
			for (ManantialesFicha internalTok : facts) {
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
	private List<Move> fill (BorderType border, Set<ManantialesFicha> tokens) {
		List<Move> ret = new ArrayList<Move>();
		switch (border) {
			case NORTH:
				for (ManantialesFicha token : tokens) {
					if (token.getColor().equals(Color.BLUE)) {
						Set<ManantialesFicha> seconds = borderMap.get(BorderType.WEST);
						for (ManantialesFicha second : seconds) {
							Set<ManantialesFicha> thirds = borderMap.get(BorderType.SOUTH);
							for (ManantialesFicha third : thirds) {
								Set<ManantialesFicha> fourths = borderMap.get(BorderType.EAST);
								for (ManantialesFicha fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.GREEN)) {
						Set<ManantialesFicha> seconds = borderMap.get(BorderType.EAST);
						for (ManantialesFicha second : seconds) {
							Set<ManantialesFicha> thirds = borderMap.get(BorderType.SOUTH);
							for (ManantialesFicha third : thirds) {
								Set<ManantialesFicha> fourths = borderMap.get(BorderType.WEST);
								for (ManantialesFicha fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					}
				}
				break;
			case EAST:
				for (ManantialesFicha token : tokens) {
					if (token.getColor().equals(Color.GREEN)) {
						Set<ManantialesFicha> seconds = borderMap.get(BorderType.NORTH);
						for (ManantialesFicha second : seconds) {
							Set<ManantialesFicha> thirds = borderMap.get(BorderType.WEST);
							for (ManantialesFicha third : thirds) {
								Set<ManantialesFicha> fourths = borderMap.get(BorderType.SOUTH);
									for (ManantialesFicha fourth : fourths) {
										ret.add(new RotateMove(token,second,third,fourth));
									}
								}
							}
						} else if (token.getColor().equals(Color.YELLOW)) {
							Set<ManantialesFicha> seconds = borderMap.get(BorderType.SOUTH);
							for (ManantialesFicha second : seconds) {
								Set<ManantialesFicha> thirds = borderMap.get(BorderType.WEST);
								for (ManantialesFicha third : thirds) {
									Set<ManantialesFicha> fourths = borderMap.get(BorderType.NORTH);
									for(ManantialesFicha fourth : fourths) {
										ret.add(new RotateMove(token,second,third,fourth));
									}
								}
							}
						}
					}
				break;
			case SOUTH:
				for (ManantialesFicha token : tokens) {
					if (token.getColor().equals(Color.RED)) {
						Set<ManantialesFicha> seconds = borderMap.get(BorderType.WEST);
						for (ManantialesFicha second : seconds) {
							Set<ManantialesFicha> thirds = borderMap.get(BorderType.NORTH);
							for (ManantialesFicha third : thirds) {
								Set<ManantialesFicha> fourths = borderMap.get(BorderType.EAST);
								for (ManantialesFicha fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.YELLOW)) {
						Set<ManantialesFicha> seconds = borderMap.get(BorderType.EAST);
						for (ManantialesFicha second : seconds) {
							Set<ManantialesFicha> thirds = borderMap.get(BorderType.NORTH);
							for (ManantialesFicha third : thirds) {
								Set<ManantialesFicha> fourths = borderMap.get(BorderType.WEST);
								for (ManantialesFicha fourth : fourths) {
									ret.add(new RotateMove(token, second, third, fourth));
								}
							}
						}
					}
				}
				break;
			case WEST:
				for (ManantialesFicha token : tokens) {
					if (token.getColor().equals(Color.RED)) {
						Set<ManantialesFicha> seconds = borderMap.get(BorderType.SOUTH);
						for (ManantialesFicha second : seconds) {
							Set<ManantialesFicha> thirds = borderMap.get(BorderType.EAST);
							for (ManantialesFicha third : thirds) {
								Set<ManantialesFicha> fourths = borderMap.get(BorderType.NORTH);
								for (ManantialesFicha fourth : fourths) {
									ret.add(new RotateMove(token,second,third,fourth));
								}
							}
						}
					} else if (token.getColor().equals(Color.BLUE)) {
						Set<ManantialesFicha> seconds = borderMap.get(BorderType.NORTH);
						for (ManantialesFicha second : seconds) {
							Set<ManantialesFicha> thirds = borderMap.get(BorderType.EAST);
							for (ManantialesFicha third : thirds) {
								Set<ManantialesFicha> fourths = borderMap.get(BorderType.SOUTH);
								for (ManantialesFicha fourth : fourths) {
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
