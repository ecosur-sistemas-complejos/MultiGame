/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.gente;

import java.util.HashSet;
import java.util.TreeSet;

import javax.persistence.Entity;

import org.drools.RuleBase;
import org.drools.StatefulSession;

import mx.ecosur.multigame.MessageSender;

import mx.ecosur.multigame.impl.CellComparator;
import mx.ecosur.multigame.impl.Color;

import mx.ecosur.multigame.impl.enums.gente.GenteStrategy;

import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridGame;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.impl.model.gente.BeadString;
import mx.ecosur.multigame.impl.model.gente.PenteMoveComparator;

import mx.ecosur.multigame.impl.util.Direction;
import mx.ecosur.multigame.impl.util.Search;

import mx.ecosur.multigame.model.implementation.AgentImpl;

@Entity
public class GenteStrategyAgent extends GentePlayer implements AgentImpl {
	
	private static final long serialVersionUID = 6999849272112074624L;
	
	private GenteStrategy strategy;
	
	private GenteMove nextMove;
	
	public GenteStrategyAgent () {
		super();
		nextMove = null;
	}
	
	public GenteStrategyAgent (GridGame game, GridRegistrant player, Color color, 
			GenteStrategy strategy) 
	{
		super (game, player, color);
		this.strategy = strategy;
	}
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.AgentImpl#initialize()
	 */
	public void initialize() {
		nextMove = null;
	}

	public GenteStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(GenteStrategy strategy) {
		this.strategy = strategy;
	}
	
	/**
	 * Returns a list of cells that are open and adjacent to cells of 
	 * the colors, "colors".
	 * 
	 * @param colors
	 * @return
	 */
	private TreeSet<GridCell> findUnboundAdjacentCells (HashSet<Color> colors) {
		TreeSet<GridCell> ret = new TreeSet<GridCell> (new CellComparator());
		TreeSet<GridCell> candidates = new TreeSet<GridCell> (new CellComparator());
		Search search = new Search (game.getGrid());
		
		/* Get all Cells of with the targeted Colors */
		for (GridCell cell : game.getGrid().getCells ()) {
			if (colors.contains(cell.getColor())) {
				candidates.add(cell);
			}
		}
		
		/* Do a search with a depth of 1 for all of those cells, if a null
		 * is returned, create a new cell and add it to the unbound list */
		for (GridCell candidate : candidates) {
			for (Direction direction : Direction.values()) {
				if (direction == Direction.UNKNOWN)
					continue;
				GridCell result = search.searchGrid(direction, candidate, 1);
				if (result == null) {
					ret.add(createDestination (direction, candidate, 1));
				}
			}
		}
		
		return ret;
	}
	
	public TreeSet<GenteMove> determineScoringMoves (Color color) {
		HashSet<Color> colors = new HashSet<Color> ();
		colors.add(color);
		return determineScoringMoves (colors);
	}
	
	/**
	 * Returns a list of cells that would result in a Tria or Tessera for the 
	 * colors, "colors".
	 * 
	 * @param colors
	 * @return
	 */
	public TreeSet<GenteMove> determineScoringMoves (HashSet<Color> colors) {
		TreeSet<GenteMove> ret = new TreeSet<GenteMove>(new PenteMoveComparator());
			/* Get the unbound adjacents, and speculate on moves */
		TreeSet<GridCell> unbound = this.findUnboundAdjacentCells(colors);
		for (GridCell cell : unbound){
			for (Color color : colors) {
				cell.setColor(color);
				GenteMove move = new GenteMove (this, cell);
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
	
	public TreeSet<GenteMove> determineAvailableMoves () {
		return this.determineAvailableMoves (this.getColor());
	}
	
	public TreeSet<GenteMove> determineAvailableMoves (HashSet<Color> colors) {
		TreeSet<GenteMove> ret = new TreeSet<GenteMove>(new PenteMoveComparator());
		TreeSet<GridCell> unbound = this.findUnboundAdjacentCells(colors);
		for (GridCell cell : unbound){
			for (Color color : colors) {
				cell.setColor(color);
				ret.add(new GenteMove (this, cell));
			}
		}
		
		return ret;
	}
	
	public TreeSet<GenteMove> determineAvailableMoves (Color color) {
		HashSet<Color> colors = new HashSet<Color> ();
		colors.add(color);
		return determineAvailableMoves (colors);
	}
	
	public HashSet<Color> oppositionColors () {
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
	private GridCell createDestination(Direction direction, GridCell cell, int factor) {
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
		
		return new GridCell (column, row, Color.UNKNOWN);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.Agent#determineNextMove(mx.ecosur.multigame.model.Game)
	 */
	public GenteMove determineNextMove() {
		RuleBase ruleBase = strategy.getRuleBase();
		StatefulSession statefulSession = ruleBase.newStatefulSession();
		statefulSession.insert(this);
		statefulSession.insert(game);
		statefulSession.insert(new MessageSender());
		statefulSession.fireAllRules();
		statefulSession.dispose();
		/* Rules should have created the next Move into this player, if 
		 * one available */
		GenteMove ret = (GenteMove) getNextMove ();
		if (ret != null) {
			GridCell destination = (GridCell) ret.getDestination();
			destination.setColor(getColor());
			ret.setDestination(destination);
		}
		
		return ret;	
	}
	
	public void setNextMove (GenteMove next) {
		this.nextMove = next;
	}
	
	public GenteMove getNextMove () {
		return nextMove;
	}
}
