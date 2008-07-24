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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.Direction;
import mx.ecosur.multigame.Vertice;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.pente.BeadString;

/**
 * @author awater
 *
 */
@Entity
@DiscriminatorValue("PENTE")
@NamedQueries( { 
	@NamedQuery(name = "getPenteMoves", query = "select pm from PenteMove pm where pm.player.game=:game order by pm.id asc") 
})
public class PenteMove extends Move {
	
	public enum CooperationQualifier {
		COOPERATIVE, SELFISH, NEUTRAL
	}

	private HashSet<Cell> captures;
	
	private HashSet<BeadString> trias, tesseras;

	private CooperationQualifier qualifier;
	
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
					Cell result = searchGrid (direction, cell, 
							getDestination().getColor(), 1);
					if (result != null) {
						captures.addAll (trapped);
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
			Map<Vertice, BeadString> stringMap = getString (3);
			for (Vertice v: stringMap.keySet()) {
				BeadString string = stringMap.get(v);
				if (uncountedString (string) && string.contiguous(v))
					trias.add(string);
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
			Map<Vertice, BeadString> stringMap = getString (4, true);
			for (Vertice v : stringMap.keySet()) {
				BeadString string = stringMap.get(v); 
				if (uncountedString (string) && string.contiguous(v))
					tesseras.add(string);
			}
		}
		
		return tesseras;
	}
	
	public void setTesseras (HashSet<BeadString> tesseras) {
		this.tesseras = tesseras;
	}
	
	private boolean uncountedString(BeadString string) {
		boolean ret = false;
		PentePlayer player = (PentePlayer) getPlayer();
		if (!player.containsString(string))
			ret = true;
		return ret;
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
	
	/**
	 * Gets the qualifier
	 * 
	 * @return the qualifier
	 */
	@Enumerated(EnumType.STRING)
	public CooperationQualifier getQualifier() {
		return qualifier;
	}

	/**
	 * Sets the cooperation qualifier
	 * 
	 * @param qualifier
	 *            the cooperation qualifier
	 */
	public void setQualifier(CooperationQualifier qualifier) {
		this.qualifier = qualifier;
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
		 * adjacent directions to the requested depth, otherwise 
		 * search the grid in the named direction to the requested
		 * depth.
		 */
		if (startingCell.getDirection() == Direction.UNKNOWN) {
			Set<AnnotatedCell> adjacent = findAdjacentCells(targetColors);
			/* Add the adjacent cells to the candidate list */
			ret.addAll(adjacent);
			
			for (AnnotatedCell direction : adjacent) {
				ret.addAll(findCandidates (direction, targetColors, depth));
			}
		} else {
			for (int i = 0; i < depth - 1; i++) {
				for (Color targetColor : targetColors) {
						/* Search the grid to the depth of current, + 1 */
					Cell result = this.searchGrid(startingCell.getDirection(), 
							startingCell.getCell(), targetColor, i + 1);
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
	
	/*
	 * Check each cell next to the destination, if
	 * a cell is found in that direction, add that 
     * cell to the list.
	 */
	private Set<AnnotatedCell> findAdjacentCells(Color[] targetColors) {
		HashMap <AnnotatedCell,Direction> adjacent = new HashMap <AnnotatedCell,Direction> ();
		for (Direction d : Direction.values()) {
			for (Color color : targetColors) {
				Cell result = this.searchGrid (d,
					getDestination(), color, 1);
				AnnotatedCell annotatedResult = new AnnotatedCell (result,d);
				if (result != null && !adjacent.containsValue(d))
					adjacent.put (annotatedResult,d);
			}
		}
		
		return adjacent.keySet();
	}

	/*
	 * Returns a Cell to be searched for within a grid, with a factor of 
	 * change defined by "factor".  North is considered the "top" of the 
	 * board, meaning the lowest row.  All other cardinal points are derived 
	 * from this simple map.
	 * @param direction
	 * @param cell
	 * @param factor
	 * @return
	 */
	private Cell searchGrid(Direction direction, Cell cell, Color color, int factor) {
		int row = 0, column = 0;
		switch (direction) {
			case NORTH:
				column = cell.getColumn();
				row = cell.getRow() - factor;
				break;
			case SOUTH:
				column = cell.getColumn();
				row = cell.getRow() + factor;
				break;
			case EAST:
				column = cell.getColumn() + factor;
				row = cell.getRow();
				break;
			case WEST:
				column = cell.getColumn () - factor;
				row = cell.getRow();
				break;
			case NORTHEAST:
				column = cell.getColumn() + factor;
				row = cell.getRow() - factor;
				break;
			case NORTHWEST:
				column = cell.getColumn () - factor;
				row = cell.getRow() - factor;
				break;
			case SOUTHEAST:
				column = cell.getColumn() + factor;
				row = cell.getRow () + factor;
				break;	
			case SOUTHWEST:
				column = cell.getColumn () - factor;
				row = cell.getRow() + factor;
				break;
			default:
				break;

		}

		Cell searchCell = new Cell (column, row, color);
		return this.getPlayer().getGame().getGrid().getLocation(searchCell);
	}
	
	private Map<Vertice, BeadString> getString (int stringlength) {
		return getString (stringlength, false);
	}
	
	private Map<Vertice, BeadString> getString(int stringlength, boolean compliment) {
		HashMap<Vertice, BeadString> ret = new HashMap<Vertice, BeadString> ();
		
		AnnotatedCell start = new AnnotatedCell (getDestination());
			
		/* We are only interested in cells of this color */
		Color[] colors;
		if (compliment) {
			colors = new Color [ 2 ];
			colors [ 0 ] = this.getDestination().getColor();
			colors [ 1 ] = colors [ 0 ].getCompliment();
		} else {
			colors = new Color [ 1 ];
			colors [ 0 ] = this.getDestination().getColor();			
		}
		
		/* Perform a search to the depth of stringlength + 1, to pickup 
		 * any non-valid configurations */
		Set<AnnotatedCell> candidates = findCandidates(start, colors, stringlength + 1);
		
		/* Vertice Hash of cells */
		HashMap <Vertice,BeadString> directionalMap = 
			new HashMap<Vertice,BeadString>();

		/* Sort the candidates by Vertice */
		for (AnnotatedCell candidate : candidates) {
			if (directionalMap.containsKey(candidate.getDirection().getVertice())) {
				BeadString string = directionalMap.get(candidate.getDirection().getVertice());
				string.add(candidate.getCell());
				directionalMap.put (candidate.getDirection().getVertice(), string);
			} else {
				BeadString string = new BeadString ();
				string.add(candidate.getCell());
				directionalMap.put(candidate.getDirection().getVertice(), string);
			}
		}
		
		/* Pull each list of cells from the directional map, and ensure that
		 * there are at least stringlength cells in that list. */
		for (Vertice v : directionalMap.keySet()) {
			BeadString string = directionalMap.get(v);
			if (string.size() == stringlength - 1) {
				string.add(getDestination());
			} 
			
			if (string.contains(getDestination()) && string.size() >= stringlength) {
				ret.put (v, string);
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

		@Override
		public String toString() {
			return direction.toString() + ", " + cell.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AnnotatedCell) {
				AnnotatedCell comparison = (AnnotatedCell) obj;
				return (comparison.cell.equals(this.cell) && 
						comparison.direction.equals(this.direction));
			} else
				return super.equals(obj);
		}
	}
}
