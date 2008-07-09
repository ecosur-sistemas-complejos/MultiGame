package mx.ecosur.multigame.ejb.entity;

/**
 * The Player class represents a player registered with the system. 
 * This class simply maps into the persistent layer to give the system
 * a means to track users, how many games they have won, and how many
 * played.  
 */

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@NamedQueries ({
	@NamedQuery(name="getPlayer",
			query="select p from Player p where p.name=:name")})
@Entity
public class Player implements Serializable {

	/*
	 * Primary key
	 */
	private int id;

	/*
	 * The player's name
	 */
	private String name;

	/*
	 * The number of games this player has played
	 */
	private int gamecount;

	/*
	 * The number of games this player has won
	 */
	private int wins;

	/*
	 * Last time this player logged in to play
	 */
	private long lastRegistration;

	public Player() {
		super();
	}
	
	public Player (String name) {
		super();
		this.name = name;
	}

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the gamecount
	 */
	public int getGamecount() {
		return gamecount;
	}

	/**
	 * @param gamecount
	 *            the gamecount to set
	 */
	public void setGamecount(int gamecount) {
		this.gamecount = gamecount;
	}

	/**
	 * @return the wins
	 */
	public int getWins() {
		return wins;
	}

	/**
	 * @param wins
	 *            the wins to set
	 */
	public void setWins(int wins) {
		this.wins = wins;
	}

	/**
	 * @return the lastRegistration
	 */
	public long getLastRegistration() {
		return lastRegistration;
	}

	/**
	 * @param lastRegistration
	 *            the lastRegistration to set
	 */
	public void setLastRegistration(long lastRegistration) {
		this.lastRegistration = lastRegistration;
	}

	public String toString() {
		return "id = " + id + ", name = " + name + ", gamecount = " + gamecount
				+ ", lastRegistration = " + lastRegistration + ", wins = "
				+ wins;
	}
}
