package mx.ecosur.multigame.ejb.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

import mx.ecosur.multigame.Cell;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("MOVE")
public class Move implements Serializable {

	private int id;
	private GamePlayer player;
	private Cell current, destination;

	public enum Status {
		INVALID, VERIFIED, UNVERIFIED, MOVED
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

	@Column(name = "DESTINATION_CELL")
	public Cell getDestination() {
		return this.destination;
	}

	public void setDestination(Cell destination) {
		this.destination = destination;
	}

	@Column(name = "CURRENT_CELL")
	public Cell getCurrent() {
		return this.current;
	}

	public void setCurrent(Cell current) {
		this.current = current;
	}

	@OneToOne
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
