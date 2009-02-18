/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.entity.manantiales;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.manantiales.TokenType;

/**
 * The ManantialesGame represents the Game of Manantiales, played with 4 
 * players on a 4x8x4 grid (with 8 sites located in the center, and all other 
 * rows having 5 columns, split into 4 rows.  The first and last two rows
 * are specific to 
 * 
 */

public class ManantialesGame extends Game implements Cloneable {
	
	private static final long serialVersionUID = -2117368168251380686L;
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.entity.Game#initialize(mx.ecosur.multigame.GameType)
	 */
	@Override
	public void initialize(GameType type) {
		super.initialize(type);
		/* Set the initial board up with all undeveloped territory */
		SortedSet<Cell> tokens = new TreeSet<Cell>(new CellComparator());
		for (int col = 0; col < 9; col++) {
			for (int row = 0; row < 9; row++) {
				if (row == 4 && col ==4)
					continue;
				Color color = null;
					/* All tokens across row 4 are set (except for the manantial) */
				if (row == 4 && col!=4) {
					if (col < 5) {
						color = Color.RED;
					} else
						color = Color.GREEN;
					tokens.add(new Token(col,row, color, TokenType.UNDEVELOPED));
					/* Cells are split by even/even and odd/odd (skip manantial) */
				} else if ( (row !=4 && col!=4) && ( 
						(col % 2 ==0 && row % 2 == 0) || (col % 2 !=0 && row % 2 !=0))) 
				{
					if (row < 4 && col < 5) 
						color = Color.BLUE;
					else if (row < 4 && col > 4)
						color = Color.GREEN;
					else if (row > 4 && col < 4)
						color = Color.RED;
					else if (row > 4 && col > 3)
						color = Color.YELLOW;
					tokens.add(new Token (col,row, color, TokenType.UNDEVELOPED));
				} else if (col == 4) {
					if (row < 5 ) 
						color = Color.BLUE;
					else if (row > 4)
						color = Color.YELLOW;
					tokens.add (new Token (col, row, color, TokenType.UNDEVELOPED));
				} else
					continue;
			}
		}
		
		grid.setCells(tokens);
	}
	
	/**
	 * Evaluates the score of a particular player (denoted by Color).
	 * @param color
	 * @return
	 */
	public int getScore (Color color) {
		int ret = 0;
		/* Accumulate the score from all colored tokens */
		for (Cell cell : this.getGrid().getCells()) {
			if (cell.getColor().equals(color)) {
				Token token = (Token) cell;
				ret += token.getType().value();
			}
		}
		
		return ret;
	}
	
	/**
	 * Evaluates the total number of premiums awarded in the game.
	 * @return
	 */
	public int getPremiums () {
		int ret = 0;
		int intensives = 0, forested = 0, silvo = 0;
		for (Cell cell : this.getGrid().getCells()) {
			Token token = (Token) cell;
			switch (token.getType()) {
				case MANAGED_FOREST:
					break;
				case INTENSIVE_PASTURE:
					break;
				case SILVOPASTORAL:
					break;
				default:
					continue;
			}
			
			ret = intensives + forested + silvo;
		}		
		
		return ret;
	}
	
	public int getDeforestation () {
		int ret = 0;
		/* Accumulate the number of deforested pieces by tokens that 
		 * deforest */
		for (Cell cell : this.getGrid().getCells()) { 
			Token token = (Token) cell;
			switch (token.getType()) {
				case MODERATE_PASTURE:
					ret += 1;
					break;
				case INTENSIVE_PASTURE:
					ret += 1;
					break;
				case SILVOPASTORAL:
					ret += 1;
					break;
				default:
					continue;
			}
		}
		return ret;
	}
	
	public SortedSet<Token> getTokens () {
		SortedSet<Token> ret = new TreeSet<Token>(new CellComparator());
		for (Cell cell : grid.getCells()) {
			ret.add((Token) cell);
		}
		
		return ret;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ManantialesGame ret = new ManantialesGame();
		ret.setColumns(getColumns());
		ret.setCreated(new Date(System.currentTimeMillis()));
		ret.setGrid((GameGrid) getGrid().clone());
		ret.setId(0);
		ret.setPlayers(getPlayers());
		ret.setRows(getRows());
		ret.setState(GameState.valueOf(getState().toString()));
		ret.setType(GameType.valueOf(getType().toString()));
		ret.setVersion(getVersion());
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getState().toString() + "\n");
		buf.append ("Population = " + getGrid().getCells().size() +"\n");
		for (Cell cell : getGrid().getCells()) {
			Token tok = (Token) cell;
			buf.append(tok.toString() + " ");
		}
		buf.append ("\n");
		return buf.toString();
	}	
}
