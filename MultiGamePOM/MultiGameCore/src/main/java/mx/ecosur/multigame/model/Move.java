/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * A move is an object which contains a GamePlayer, a destination cell, and 
 * optionally, an originating cell. Moves are processed by the SharedBoard
 * and when successful, are integrated into a specific game's GameGrid.  
 * Success and integration are determined by each game's rule set.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.model;

import java.io.Serializable;

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

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries( { 
	@NamedQuery(name = "getMoves", query = "select m from Move m where m.player.game=:game order by m.id asc") 
})
public class Move implements Serializable {

	private static final long serialVersionUID = 8017901476308051472L;
	private int id;
	protected GamePlayer player;
	protected Cell current;
	protected Cell destination;

	public enum Status {
		INVALID, VERIFIED, UNVERIFIED, MOVED, EVALUATED
	}

	private Status status;

	public Move() {
		super();
		this.status = Status.UNVERIFIED;
	}

	public Move(GamePlayer player, Cell destination) {
		this.player = player;
		this.current = null;
		this.destination = destination;
		this.status = Status.UNVERIFIED;
	}

	public Move(GamePlayer player, Cell current, Cell destination) {
		this.player = player;
		this.current = current;
		this.destination = destination;
		this.status = Status.UNVERIFIED;
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
	public Cell getDestination() {
		return this.destination;
	}

	public void setDestination(Cell destination) {
		this.destination = destination;
	}

	@OneToOne  (cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
	public Cell getCurrent() {
		return this.current;
	}

	public void setCurrent(Cell current) {
		this.current = current;
	}

	@OneToOne  (cascade={CascadeType.ALL},fetch=FetchType.EAGER)
	public GamePlayer getPlayer() {
		return this.player;
	}

	public void setPlayer(GamePlayer player) {
		this.player = player;
	}

	/**
	 * @return the status
	 */
	@Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	public String toString() {
		return "Player: " + player + "\nCurrent: " + current
				+ "\nDestination: " + destination + "\nStatus: " + status
				+ "\nGame: " + player.getGame();
	}
}
