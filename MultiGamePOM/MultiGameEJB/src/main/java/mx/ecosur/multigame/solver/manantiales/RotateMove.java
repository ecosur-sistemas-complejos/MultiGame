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

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.manantiales.Ficha;
import mx.ecosur.multigame.manantiales.BorderType;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.move.Move;

/*
 * Rotates tokens of differing colors around the board, for one rotation.
 * This move is only valid if all tokens are border tokens.
 */
public class RotateMove implements Move {
	
	private Ficha primary, second, third, fourth;
	private Direction direction;
	
	enum Direction {
		CLOCKWISE, COUNTERCLOCKWISE, UNKNOWN;
	}
	
	public RotateMove (Ficha primary, Ficha second, Ficha third, Ficha fourth) {
		this.primary = primary;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
		this.direction = determineDirection(primary, second);
	}

	/**
	 * @return
	 */
	private Direction determineDirection(Ficha origin, Ficha destination) {
		Direction ret = Direction.UNKNOWN;
		switch (origin.getColor()) {
			case BLUE:
				if (destination.getColor().equals(Color.RED))
					ret = Direction.COUNTERCLOCKWISE;
				else if (destination.getColor().equals(Color.GREEN))
					ret = Direction.CLOCKWISE;
				break;
			case GREEN:
				if (destination.getColor().equals(Color.BLUE))
					ret = Direction.COUNTERCLOCKWISE;
				else if (destination.getColor().equals(Color.YELLOW))
					ret = Direction.CLOCKWISE;
				break;
			case YELLOW:
				if (destination.getColor().equals(Color.GREEN))
					ret = Direction.COUNTERCLOCKWISE;
				else if (destination.getColor().equals(Color.RED))
					ret = Direction.CLOCKWISE;
				break;
			case RED:
				if (destination.getColor().equals(Color.BLUE))
					ret = Direction.COUNTERCLOCKWISE;
				else if (destination.getColor().equals(Color.YELLOW))
					ret = Direction.CLOCKWISE;
				break;
			default:
				break;
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#createUndoMove(org.drools.WorkingMemory)
	 */
	public Move createUndoMove(WorkingMemory workingMemory) {
		return new RotateMove (fourth, third, second, primary);
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#doMove(org.drools.WorkingMemory)
	 */
	public void doMove(WorkingMemory wm) {
		int primaryRow, primaryColumn, secondRow, secondColumn, thirdRow, thirdColumn,
			fourthColumn, fourthRow;
		
		primaryRow = primary.getRow();
		primaryColumn = primary.getColumn();
		
		secondRow = second.getRow();
		secondColumn = second.getColumn();
		
		thirdRow = third.getRow();
		thirdColumn = third.getColumn();
		
		fourthRow = fourth.getRow();
		fourthColumn = fourth.getColumn();
		
		FactHandle primaryFH, secondFH, thirdFH, fourthFH;
		
		primaryFH = wm.getFactHandle(primary);
		secondFH = wm.getFactHandle(second);
		thirdFH = wm.getFactHandle(third);
		fourthFH = wm.getFactHandle(fourth);
		
		/* Rotate */
		primary.setColumn(secondColumn);
		primary.setRow(secondRow);
		second.setColumn(thirdColumn);
		second.setRow(thirdRow);
		third.setColumn(fourthColumn);
		third.setRow(fourthRow);
		fourth.setColumn(primaryColumn);
		fourth.setRow(primaryRow);
		
		/* Update working memory */	
		wm.modifyRetract(primaryFH);
		wm.modifyInsert(primaryFH, primary);
		wm.modifyRetract(secondFH);
		wm.modifyInsert(secondFH, second);
		wm.modifyRetract(thirdFH);
		wm.modifyInsert(thirdFH, third);
		wm.modifyRetract (fourthFH);
		wm.modifyInsert (fourthFH, fourth);
	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.move.Move#isMoveDoable(org.drools.WorkingMemory)
	 */
	public boolean isMoveDoable(WorkingMemory workingMemory) {
		/* Rotation is correct */
		boolean ret = !(direction.equals(Direction.UNKNOWN));
		ret = ret && isAcrossTerritory(primary,second);
		ret = ret && isAcrossTerritory(second,third);
		ret = ret && isAcrossTerritory(third,fourth);
		ret = ret && isAcrossTerritory(fourth,primary);
		
		/* All tokens are on the border */
		ret = ret && !(primary.getBorder().equals(BorderType.NONE))
			&& !(second.getBorder().equals(BorderType.NONE))
			&& !(third.getBorder().equals(BorderType.NONE))
			&& !(fourth.getBorder().equals(BorderType.NONE));
		return ret;
	}
	
	private boolean isAcrossTerritory(Ficha origin, Ficha destination) {
		boolean ret = false;
		
		switch (origin.getColor()) {
			case BLUE:
				if (direction.equals(Direction.COUNTERCLOCKWISE))
					ret = (destination.getColor().equals(Color.RED));
				else
					ret = (destination.getColor().equals(Color.GREEN));
				break;
			case GREEN:
				if (direction.equals(Direction.COUNTERCLOCKWISE))
					ret = (destination.getColor().equals(Color.BLUE));
				else
					ret = (destination.getColor().equals(Color.YELLOW));
				break;
			case YELLOW:
				if (direction.equals(Direction.COUNTERCLOCKWISE))
					ret = (destination.getColor().equals(Color.GREEN));
				else
					ret = (destination.getColor().equals(Color.RED));
				break;
			case RED:
				if (direction.equals(Direction.COUNTERCLOCKWISE))
					ret = (destination.getColor().equals(Color.YELLOW));
				else
					ret = (destination.getColor().equals(Color.BLUE));
				break;
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "rotate [" + direction + "] " + primary + " => " + second + "=>" + third + "=>" + 
			fourth + "=>" + primary;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof RotateMove) {
			RotateMove comparison = (RotateMove) obj;
				/* Moves are equal if it is the same change */
			ret = (comparison.primary.equals(this.primary) 
					&& comparison.second.equals(this.second)
					&& comparison.third.equals(this.third) 
					&& comparison.fourth.equals(this.fourth));
		} else {
			ret = super.equals(obj);
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder() 
        .append(primary) 
        .append(second)
        .append(third)
        .append(fourth)
        .toHashCode(); 
	}
	

}
