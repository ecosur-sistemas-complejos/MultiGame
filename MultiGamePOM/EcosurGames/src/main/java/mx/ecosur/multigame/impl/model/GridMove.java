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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.model.implementation.CellImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries( { 
	@NamedQuery(name = "getMoves", query = "select m from Move m where m.player.game=:game order by m.id asc") 
})
public abstract class GridMove implements MoveImpl {

	private static final long serialVersionUID = 8017901476308051472L;
	private int id;
	protected GridPlayer player;
	protected GridCell current;
	protected GridCell destination;

	private MoveStatus status;

	public GridMove() {
		super();
		this.status = MoveStatus.UNVERIFIED;
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
	public CellImpl getDestination() {
		return this.destination;
	}

	public void setDestination(CellImpl destination) {
		this.destination = (GridCell) destination;
	}

	@OneToOne  (cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
	public CellImpl getCurrent() {
		return this.current;
	}

	public void setCurrent(CellImpl current) {
		this.current = (GridCell) current;
	}

	@OneToOne  (cascade={CascadeType.ALL},fetch=FetchType.EAGER)
	public GridPlayer getPlayer() {
		return this.player;
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

	public String toString() {
		return "Registrant: " + player + "\nCurrent: " + current
				+ "\nDestination: " + destination + "\nStatus: " + status
				+ "\nGame: " + player.getGame();
	}
}
