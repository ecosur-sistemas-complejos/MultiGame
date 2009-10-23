/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * A move is an object which contains a GridRegistrant, a destination cell, and 
 * optionally, an originating cell. Moves are processed by the SharedBoard
 * and when successful, are integrated into a specific game's GameGrid.  
 * Success and integration are determined by each game's rule set.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl.model;

import javax.persistence.*;

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.implementation.CellImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class GridMove implements MoveImpl, Cloneable {

	private static final long serialVersionUID = 8017901476308051472L;
	private int id;
	protected GridPlayer player;
	protected GridCell current;
	protected GridCell destination;

	private MoveStatus status;

	public GridMove() {
		super();
		this.status = MoveStatus.UNVERIFIED;
		this.current = null;
		this.destination = null;
		this.player = null;
	}

	public GridMove(GridPlayer player, GridCell destination) {
		this.player = player;
		this.current = null;
		this.destination = destination;
		this.status = MoveStatus.UNVERIFIED;
	}

	public GridMove(GridPlayer player, GridCell current, GridCell destination) {
		this.player = player;
		this.current = current;
		this.destination = destination;
		this.status = MoveStatus.UNVERIFIED;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@OneToOne  (cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
	public GridCell getDestinationCell() {
		return this.destination;
	}

	public void setDestinationCell(GridCell destination) {
		this.destination = destination;
	}

	@OneToOne  (cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
	public GridCell getCurrentCell() {
		return this.current;
	}

	public void setCurrentCell(GridCell current) {
		this.current = current;
	}

	@OneToOne  (cascade={CascadeType.ALL},fetch=FetchType.EAGER)
	public GridPlayer getPlayer() {
		return this.player;
	}
	
	@Transient
	public GamePlayer getPlayerModel () {
		return new GamePlayer (player);
	}
	
	@Transient
	public void setPlayerModel (GamePlayer model) {
        if (model != null && model.getImplementation() != null)
		    this.player = (GridPlayer) model.getImplementation();
	}

	public void setPlayer(GridPlayer player) {
		this.player = player;
	}

	/**
	 * @return the status
	 */
	@Enumerated(EnumType.STRING)
	public MoveStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(MoveStatus status) {
		this.status = status;
	}
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.MoveImpl#setCurrent(mx.ecosur.multigame.model.implementation.CellImpl)
	 */
	public void setCurrentCell(CellImpl cellImpl) {
		setCurrentCell((GridCell) cellImpl);
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.MoveImpl#setDestination(mx.ecosur.multigame.model.implementation.CellImpl)
	 */
	public void setDestinationCell(CellImpl cellImpl) {
		setDestinationCell((GridCell) cellImpl);
	}

	public String toString() {
		return "Registrant: " + player + "\nCurrent: " + current
				+ "\nDestination: " + destination + "\nStatus: " + status;
	}

    @Override
	protected abstract Object clone () throws CloneNotSupportedException;
	
}
