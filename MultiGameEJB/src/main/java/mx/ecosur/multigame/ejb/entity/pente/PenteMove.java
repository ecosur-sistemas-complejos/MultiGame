/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.Direction;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;

/**
 * @author awater
 *
 */
@Entity
@DiscriminatorValue("PENTE")
public class PenteMove extends Move {
	
	HashSet<Cell> captures;
	
	public PenteMove () {
		super ();
	}

	public PenteMove(Game game, Player player, Cell destination) {
		super(game, player, destination);
	}
	
	/*
	 * Returns a set of captured pieces, computed once. 
	 */
	public Set<Cell> getCaptures (Color[] candidateColors) {
		GameGrid grid = this.getGame().getGrid();
		
		if (captures == null) {
			captures = new HashSet<Cell>();
			
			/* Find 2 levels of cells in all directions, from this move */
			Set<AnnotatedCell> candidates = findCandidates (
					new AnnotatedCell (getDestination()), candidateColors, 2);
			
			/* Directional Hash of cells */
			HashMap <Direction,ArrayList<Cell>> directionalMap = 
				new HashMap<Direction,ArrayList<Cell>>();
		
			/* Sort the candidates */
			for (AnnotatedCell candidate : candidates) {
				if (directionalMap.containsKey(candidate.getDirection())) {
					ArrayList<Cell> lst = directionalMap.get(candidate.getDirection());
					lst.add(candidate.getCell());
					directionalMap.put (candidate.getDirection(), lst);
				} else {
					ArrayList<Cell> lst = new ArrayList<Cell> ();
					lst.add(candidate.getCell());
					directionalMap.put(candidate.getDirection(), lst);
				}
			}
			
			/* For each Direction, check and see if the last cell in the list
			 * is bounded by a cell of this color 
			 */
			Set<Direction> keys = directionalMap.keySet();
			for (Direction direction : keys) {
				ArrayList<Cell> trapped = directionalMap.get(direction);
				if (trapped.size() != 2) 
					continue;
				Cell cell = trapped.get(1);
				Cell result = searchGrid (grid, direction, cell);
				if (result.getColor() == getDestination().getColor()) {
					captures.addAll(trapped);
				}
			}
		}
		
		return captures;
		
	}

	/*
	 * Returns a list of annotated cells, from a starting point.  If the 
	 * annotated starting point has a known direction, then search will
	 * be performed in that direction.  If the direction is "UNKNOWN", 
	 * a search will be performed in all 8 directions to the specified depth.
	 *
	 */
	private Set<AnnotatedCell> findCandidates(AnnotatedCell startingCell, 
			Color[] targetColors, int depth) 
	{
		GameGrid grid = this.getGame().getGrid();
		Set<AnnotatedCell> ret = new HashSet<AnnotatedCell> ();
		
		/* If the annotated cell has no direction, search in all
		 * directions to the requested depth (expensive!), otherwise
		 * search the grid in the named direction.
		 */
		
		if (startingCell.getDirection() == Direction.UNKNOWN) {
			for (Direction direction : Direction.values()) {
				AnnotatedCell start = new AnnotatedCell (startingCell.getCell(), 
					direction);
				ret.addAll(findCandidates (start, targetColors, depth));
			}
		} else {
			for (int i = 0; i < depth; i++) {
				Cell result = this.searchGrid(grid, startingCell.getDirection(), 
						startingCell.getCell());
				
				for (Color targetColor: targetColors) {
					if (result != null && result.getColor() == targetColor) {
						ret.add(new AnnotatedCell (result, 
								startingCell.getDirection()));
						break;
					}	
				}
			}
		}
		
		return ret;
	}
	
	
	private Cell searchGrid(GameGrid grid, Direction direction, Cell cell) {
		int row = 0, column = 0;
		switch (direction) {
			case NORTH:
				row = cell.getRow() - 1;
				column = cell.getColumn();
				break;
			case NORTHEAST:
				row = cell.getRow() - 1;
				column = cell.getColumn() + 1;
				break;
			case EAST:
				row = cell.getRow();
				column = cell.getColumn() + 1;
				break;
			case SOUTHEAST:
				break;
			case SOUTH:
				break;
			case SOUTHWEST:
				break;
			case WEST:
				break;
			case NORTHWEST:
				break;
			default:
				break;

		}

		return grid.getLocation(row, column);
	}
	
	
	class AnnotatedCell {
		
		private Direction direction;
		
		private Cell cell;
		
		public AnnotatedCell (Cell cell) {
			this.cell = cell;
			this.direction = Direction.UNKNOWN;
		}
		
		public AnnotatedCell (Cell cell, Direction direction) {
			this.cell = cell;
			this.direction = direction;
		}

		public Direction getDirection() {
			return direction;
		}

		public void setDirection(Direction direction) {
			this.direction = direction;
		}

		public Cell getCell() {
			return cell;
		}
	}
}
