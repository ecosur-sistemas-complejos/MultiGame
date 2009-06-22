/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * A Game stores stateful information about a running game. E.G., the grid's 
 * dimensions, the list of active Cells, and the Players involved.
 * 
 * @author awaterma@ecosur.mx
 *
 */

package mx.ecosur.multigame.ejb.entity;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import mx.ecosur.multigame.Agent;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;

import mx.ecosur.multigame.ejb.entity.GamePlayer;

/**
 * @author maxmil
 *
 */
@NamedQueries( {
	@NamedQuery(name = "getGameByType", query = "select g from Game g where g.type=:type "
			+ "and g.state =:state"),
	@NamedQuery(name = "getGameById", query = "select g from Game g where g.id=:id"),
	@NamedQuery(name = "getGameByTypeAndPlayer", query = "select gp.game from GamePlayer as gp "
			+ "where gp.player=:player and gp.game.type=:type and gp.game.state <>:state"),
	@NamedQuery(name = "getGamesByPlayer", query = "select gp.game from GamePlayer as gp "
				+ "where gp.player=:player and gp.game.state <> :state"),
	@NamedQuery(name = "getGamesByNotPlayer", 
			query = "select g from Game as g where g.state = :state and not exists " +
					"(select gp from GamePlayer as gp where " +
					"gp.player = :player and gp.game.id = g.id)")})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Game implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8785194013529881730L;

	/**
	 * Primary Key
	 */
	private int id;
	
	/**
	 * Game's Type
	 */
	private GameType type;
	
	/**
	 * The date the game was created
	 */
	private Date created;
	
	/**
	 * The players involved in the game 
	 */
	private List<GamePlayer> players;
	
	protected GameGrid grid;
	
	private GameState state;
	
	private long version;
	
	/* Dimensioning for storage */
	private int rows, columns;
	
	public Game () {
		super();
		players = new ArrayList<GamePlayer> ();	
	}
	
	public abstract Game initialize (GameType type);
	
	/**
	 * @return the id
	 */
	@Id @GeneratedValue
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the type
	 */
	@Enumerated (EnumType.STRING)
	public GameType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	
	public void setType(GameType type) {
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	@Transient
	public Set getFacts () {
		Set ret = new HashSet();
		ret.addAll(grid.getCells());
		return ret;
	}

	/**
	 * @return the players
	 */
	@OneToMany (mappedBy="game", cascade={CascadeType.ALL},
			fetch=FetchType.EAGER)
	public List<GamePlayer> getPlayers() {
		return players;
	}

	/**
	 * @param players the players to set
	 */
	public void setPlayers(List<GamePlayer> players) {
		this.players = players;
	}

	/**
	 * @return the grid
	 */
	@OneToOne  (cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
	public GameGrid getGrid() {
		return grid;
	}

	/**
	 * @param grid the grid to set
	 */
	public void setGrid(GameGrid grid) {
		this.grid = grid;
	}

	/**
	 * @return the state
	 */
	@Enumerated (EnumType.STRING)
	public GameState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(GameState state) {
		this.state = state;
	}
	
	/**
	 * The number of rows of the grid contained by this game. 
	 * @return
	 */
	@Column (name="nRows")
	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}
	
	@Version
	public long getVersion () {
		return version;
	}
	
	public void setVersion (long version) {
		this.version = version;
	}
	
	/**
	 * The number of columns in the grid contained by this game.
	 * @return
	 */

	@Column (name="nColumns")
	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	/**
	 * @return the creation date of this game
	 */
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreated() {
		return created;
	}

	/**
	 * Setter for the creation date of this game
	 * 
	 * @param created
	 */
	public void setCreated(Date created) {
		this.created = created;
	}
	
	/** Non bean methods **/
	public Dimension getSize() {
		return new Dimension (rows, columns);
	}
	
	public void addPlayer (Agent agent) {
		if (!(agent instanceof GamePlayer)) 
			throw new RuntimeException (
					"Agents must be GamePlayers to be added to a game!");
		
		GamePlayer player = (GamePlayer) agent;		
		if (players == null) {
			players = new ArrayList<GamePlayer> ();
		}
		
		int max = getMaxPlayers();
		if (players.size() == max)
			throw new RuntimeException ("Maximum Players reached!");
		players.add(player);
		
		/* If we've reached the max, then set the GameState to begin */
		if (players.size() == max)
			state = GameState.BEGIN;
	}
	
	public void updatePlayer (GamePlayer player) {
		players.remove(player);
		players.add(player);
	}
	
	public void removePlayer(GamePlayer player) {
		this.players.remove(player);
	}
	
	public abstract int getMaxPlayers ();

	/**
	 * @param query
	 * @return
	 */
	public Query setParameters(Query query) {
		query.setParameter("game", this);
		return query;
	}
}