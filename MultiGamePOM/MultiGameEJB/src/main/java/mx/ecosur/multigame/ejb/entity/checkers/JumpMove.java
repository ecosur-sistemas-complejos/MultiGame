/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * A JumpMove is a special type of move for use in Checkers games.  JumpMoves
 * have an origin and a destination, along with a series of Move objects
 * that hold each specific jump of the series.
 * 
 *	@author awaterma@ecosur.mx 
 */
package mx.ecosur.multigame.ejb.entity.checkers;

import java.util.ArrayList;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;

@Entity
@DiscriminatorValue("CHECKERS")
public class JumpMove extends Move {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -669208640863689013L;
	ArrayList<Move> jumps;
	private int id;
	
	public JumpMove () {
		super ();
		jumps = new ArrayList<Move>();
	}
	
	public JumpMove(GamePlayer player, Cell current, Cell destination) {
		super(player, current, destination);
		jumps = new ArrayList<Move> ();
		jumps.add(this);
	}
	
	@Id
	@GeneratedValue
	public int getId() {
		return this.id;
	}
	
	public void setId (int id) {
		this.id = id;
	}
	
	public void addJump (Move move) {
		jumps.add(move);
	}
	
	public ArrayList<Move> getJumps () {
		return jumps;
	}
	
	/* 
	 * Calculates the position of the cell diagonally 
	 * in-between this move's current position and
	 * destination
	 */
	
	public Cell getEnemy (Move move) {
		int enemy_row, enemy_column;
		Cell start,end;
		
		start = move.getCurrent();
		end   = move.getDestination();
		
		if (start.getRow() < end.getRow ())
			enemy_row = end.getRow() - 1;
		else 
			enemy_row = end.getRow() + 1;

		if (start.getColumn() < end.getColumn())
			enemy_column = end.getColumn() - 1;
		else
			enemy_column =end.getColumn() -1;
		
		return new Cell (enemy_row, enemy_column, Color.UNKNOWN);
	}
	
}
