/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.Direction;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.pente.BeadString;

/**
 * @author awater
 *
 */
@Entity
@DiscriminatorValue("PENTE")
public class PenteMove extends Move {
	
	HashSet<Cell> captures;
	
	HashSet<BeadString> trias, tesseras;
	
	public PenteMove () {
		super ();
	}

	public PenteMove(PentePlayer player, Cell destination) {
		super(player, destination);
	}
	
	/*
	 * Returns a set of captured pieces, computed once. 
	 */
	@Transient
	public Set<Cell> getCaptures () {
		if (captures == null) {
			captures = new HashSet<Cell>();
			
			/* Find 2 levels of cells in all directions, from this move */
			Color [] candidateColors = getCandidateColors ();
			for (Color c : candidateColors) {
				Color [] searchColors = { c };
				Set<AnnotatedCell> candidates = findCandidates (
					new AnnotatedCell (getDestination()), searchColors, 2);
			
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
					Cell result = searchGrid (direction, cell);
					if (result.getColor() == getDestination().getColor()) {
						captures.addAll(trapped);
					}
				}
			}
		}
		
		return captures;
		
	}
	
	/**
	 * Gets the Trias that this move created.
	 */
	public HashSet<BeadString> getTrias () {
		if (trias == null) {
			trias = new HashSet<BeadString> ();
			Map<Direction, BeadString> stringMap = getString (3);
			for (Direction d: stringMap.keySet()) {
				trias.add(stringMap.get(d));
			}
		}
		
		return trias;
	}
	
	public void setTrias (HashSet<BeadString> trias) {
		this.trias = trias;
	}
	
	/**
	 * Gets the Tesseras that this move created.  This search will include
	 * this color's compliment.
	 * 
	 * NOTE:  This call works with the GENTE rules.  Eligible candidate for
	 * refactoring.
	 */
	public HashSet<BeadString> getTesseras () {
		if (tesseras == null) {
			tesseras = new HashSet<BeadString> ();
			Map<Direction, BeadString> stringMap = getString (4);
			for (Direction d: stringMap.keySet()) {
				tesseras.add(stringMap.get(d));
			}
		}
		
		return tesseras;
	}	
	
	public void setTesseras (HashSet<BeadString> tesseras) {
		this.tesseras = tesseras;
	}

	private Color[] getCandidateColors() {
		Color [] ret;
		
		List<GamePlayer> players = getPlayer().getGame().getPlayers();
		ArrayList<Color> colors = new ArrayList<Color>();
		ret = new Color [ players.size() -1 ];
		for (GamePlayer p : players) {
			if (p.equals(this.getPlayer()))
				continue;
			colors.add(p.getColor());
		}
		
		return colors.toArray(ret);
		
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
		Set<AnnotatedCell> ret = new HashSet<AnnotatedCell> ();
		
		/* If the annotated cell has no direction, search in all
		 * directions to the requested depth (expensive!), otherwise
		 * search the grid in the named direction.
		 */
		
		if (startingCell.getDirection() == Direction.UNKNOWN) {
			for (Direction direction : Direction.values()) {
				if (direction == Direction.UNKNOWN)
					break;
				AnnotatedCell start = new AnnotatedCell (startingCell.getCell(), 
					direction);
				ret.addAll(findCandidates (start, targetColors, depth));
			}
		} else {
			for (int i = 0; i < depth; i++) {
				Cell result = this.searchGrid(startingCell.getDirection(), 
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
	
	
	private Cell searchGrid(Direction direction, Cell cell) {
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
				row = cell.getRow () + 1;
				column = cell.getColumn() + 1;
				break;
			case SOUTH:
				row = cell.getRow() + 1;
				column = cell.getColumn();
				break;
			case SOUTHWEST:
				row = cell.getRow() + 1;
				column = cell.getColumn () -1;
				break;
			case WEST:
				row = cell.getRow();
				column = cell.getColumn () - 1;
				break;
			case NORTHWEST:
				row = cell.getRow() - 1;
				column = cell.getColumn () - 1;
				break;
			default:
				break;

		}

		return this.getPlayer().getGame().getGrid().getLocation(row, column);
	}
	
	private Map<Direction, BeadString> getString (int stringlength) {
		return getString (stringlength, false);
	}
	
	private Map<Direction, BeadString> getString(int stringlength, boolean compliment) {
		HashMap<Direction, BeadString> ret = new HashMap<Direction, BeadString> ();
		
		AnnotatedCell start = new AnnotatedCell (getDestination());
			/* We are only interested in cells of this color */
		
		Color[] colors;
		if (compliment) {
			colors = new Color [ 1 ];
			colors [ 0 ] = this.getDestination().getColor();
		} else {
			colors = new Color [ 2 ];
			colors [ 0 ] = this.getDestination().getColor();
			colors [ 1 ] = colors [ 0 ].getCompliment();
		}
		/* Perform a search to the depth of stringlength + 1, to pickup 
		 * any non-valid configurations */
		Set<AnnotatedCell> candidates = findCandidates(start, colors, stringlength + 1);
		
		/* Directional Hash of cells */
		HashMap <Direction,BeadString> directionalMap = 
			new HashMap<Direction,BeadString>();

		/* Sort the candidates */
		for (AnnotatedCell candidate : candidates) {
			if (directionalMap.containsKey(candidate.getDirection())) {
				BeadString string = directionalMap.get(candidate.getDirection());
				string.add(candidate.getCell());
				directionalMap.put (candidate.getDirection(), string);
			} else {
				BeadString string = new BeadString ();
				string.add(candidate.getCell());
				directionalMap.put(candidate.getDirection(), string);
			}
		}
		
		/* Pull each list of cells from the directional map, and ensure that
		 * there are only stringlength - 1 cells in that list. */
		for (Direction d : directionalMap.keySet()) {
			BeadString string = directionalMap.get(d);
			if (string.size() == (stringlength -1) ) {
				string.add(getDestination());
				ret.put(d, string);
			}
		}
		
		return ret;
	}
	
	private class AnnotatedCell {
		
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
