/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.entity.pente;

import java.util.HashSet;
import java.util.TreeSet;

import javax.persistence.Entity;

import org.drools.RuleBase;
import org.drools.StatefulSession;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.pente.BeadString;
import mx.ecosur.multigame.pente.PenteMoveComparator;
import mx.ecosur.multigame.pente.PenteStrategy;
import mx.ecosur.multigame.util.Direction;
import mx.ecosur.multigame.util.Search;

@Entity
public class StrategyPlayer extends PentePlayer {
	
	private static final long serialVersionUID = 6999849272112074624L;
	
	private PenteStrategy strategy;
	
	private PenteMove nextMove;
	
	public StrategyPlayer () {
		super();
	}
	
	public StrategyPlayer (Game game, Player player, Color color, 
			PenteStrategy strategy) 
	{
		super (game, player, color);
		this.strategy = strategy;
	}

	public PenteStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(PenteStrategy strategy) {
		this.strategy = strategy;
	}

	public PenteMove getNextMove() {
		return nextMove;
	}

	public void setNextMove(PenteMove nextMove) {
		this.nextMove = nextMove;
	}

	/**
	 * @param gameId
	 * @return
	 */
	public PenteMove getMove() {
		RuleBase ruleBase = strategy.getRuleBase();
		StatefulSession statefulSession = ruleBase.newStatefulSession(false);
		statefulSession.insert(this);
		statefulSession.insert(this.game);
		statefulSession.insert(new MessageSender());
		statefulSession.fireAllRules();
		statefulSession.dispose();
		/* Rules should have created the next Move into this player */
		PenteMove ret = getNextMove ();
		/* Clear the next move for upcoming calls */
		nextMove = null;
		
		return ret;	
	}
	
	/**
	 * Returns a list of cells that are open and adjacent to cells of 
	 * the colors, "colors".
	 * 
	 * @param colors
	 * @return
	 */
	private TreeSet<Cell> getUnboundAdjacentCells (HashSet<Color> colors) {
		TreeSet<Cell> ret = new TreeSet<Cell> (new CellComparator());
		TreeSet<Cell> candidates = new TreeSet<Cell> (new CellComparator());
		Search search = new Search (game.getGrid());
		
		/* Get all Cells of with the targeted Colors */
		for (Cell cell : game.getGrid().getCells ()) {
			if (colors.contains(cell.getColor())) {
				candidates.add(cell);
			}
		}
		
		/* Do a search with a depth of 1 for all of those cells, if a null
		 * is returned, create a new cell and add it to the unbound list */
		for (Cell candidate : candidates) {
			for (Direction direction : Direction.values()) {
				if (direction == Direction.UNKNOWN)
					continue;
				Cell result = search.searchGrid(direction, candidate, 1);
				if (result == null) {
					ret.add(createDestination (direction, candidate, 1));
				}
			}
		}
		
		return ret;
	}
	
	public TreeSet<PenteMove> getScoringMoves (Color color) {
		HashSet<Color> colors = new HashSet<Color> ();
		colors.add(color);
		return getScoringMoves (colors);
	}
	
	/**
	 * Returns a list of cells that would result in a Tria or Tessera for the 
	 * colors, "colors".
	 * 
	 * @param colors
	 * @return
	 */
	public TreeSet<PenteMove> getScoringMoves (HashSet<Color> colors) {
		TreeSet<PenteMove> ret = new TreeSet<PenteMove>(new PenteMoveComparator());
			/* Get the unbound adjacents, and speculate on moves */
		TreeSet<Cell> unbound = this.getUnboundAdjacentCells(colors);
		for (Cell cell : unbound){
			for (Color color : colors) {
				cell.setColor(color);
				PenteMove move = new PenteMove (this, cell);
				/* Load trias and tesseras */
				HashSet<BeadString> tesseras = move.getTesseras();
				HashSet<BeadString> trias= move.getTrias();
				if (tesseras.size() >  0) {
					ret.add(move);
				} else if (trias.size() > 0) {
					ret.add(move);
				}
			}
		}
		
		return ret;
	}
	
	public TreeSet<PenteMove> getAvailableMoves () {
		return this.getAvailableMoves (this.getColor());
	}
	
	public TreeSet<PenteMove> getAvailableMoves (HashSet<Color> colors) {
		TreeSet<PenteMove> ret = new TreeSet<PenteMove>(new PenteMoveComparator());
		TreeSet<Cell> unbound = this.getUnboundAdjacentCells(colors);
		for (Cell cell : unbound){
			for (Color color : colors) {
				cell.setColor(color);
				ret.add(new PenteMove (this, cell));
			}
		}
		
		return ret;
	}
	
	public TreeSet<PenteMove> getAvailableMoves (Color color) {
		HashSet<Color> colors = new HashSet<Color> ();
		colors.add(color);
		return getAvailableMoves (colors);
	}
	
	public HashSet<Color> getOppositionColors () {
		HashSet <Color> ret = new HashSet<Color> ();
		HashSet<Color> teamColors = new HashSet<Color>();
		teamColors.add(getColor());
		teamColors.add (getColor().getCompliment());
		for (Color color : Color.values()) {
			if (!teamColors.contains(color))
				ret.add(color);
		}
		return ret;
	}
	
	/**
	 * @param direction
	 * @param candidate
	 * @return
	 */
	private Cell createDestination(Direction direction, Cell cell, int factor) {
		int column = 0, row = 0;
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
		
		return new Cell (column, row, Color.UNKNOWN);
	}
}
