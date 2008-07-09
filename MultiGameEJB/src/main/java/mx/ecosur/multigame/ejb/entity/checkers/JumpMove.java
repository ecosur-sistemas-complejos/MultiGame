/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.checkers;

import java.util.ArrayList;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;

/**
 * @author awater
 *
 */
@Entity
@DiscriminatorValue("CHECKERS")
public class JumpMove extends Move {
	
	ArrayList<Move> jumps;
	
	public JumpMove () {
		super ();
		jumps = new ArrayList<Move>();
	}
	
	public JumpMove(GamePlayer player, Cell current, Cell destination) {
		super(player, current, destination);
		jumps = new ArrayList<Move> ();
		jumps.add(this);
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
