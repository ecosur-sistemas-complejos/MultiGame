package mx.ecosur.multigame.ejb.entity;

import java.awt.Dimension;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;

/**
 * A Game stores stateful information about a running game. E.G., the grid's 
 * dimensions, the List of active Cells, and the Players involved.
 *
 */

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="DISCRIMINATOR", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("GAME")
public class Game implements Serializable {
	
	/**
	 * Primary Key
	 */
	private int id;
	
	/**
	 * Game's Type
	 */
	private GameType type;
	
	/**
	 * The players involved in the game 
	 * @TODO Make this a Set so there can be no duplicate players
	 */
	private ArrayList<Player> players;
	
	private GameGrid grid;
	
	private GameState state;
	
	/* Dimensioning for storage */
	private int rows, columns;

	private HashMap<Date, String> messages;	
	
	public Game () {
		super();
		messages = new HashMap<Date,String>();
		players = new ArrayList<Player>();
	}

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

	/**
	 * @return the players
	 */
	@ManyToMany (fetch=FetchType.EAGER)
	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * @param players the players to set
	 */
	public void setPlayers(List<Player> players) {
		this.players = new ArrayList<Player>();
		ListIterator<Player> iter = players.listIterator();
		while (iter.hasNext()) {
			Player player = iter.next();
			this.players.add(player);
		}
	}
	
	public void addPlayer (Player player) throws RemoteException {
		if (players == null) {
			players = new ArrayList<Player> ();
		}
		
		int max = getMaxPlayers();
		if (players.size() == max)
			throw new RemoteException ("Maximum Players reached!");
		players.add(player);
		
		/* If we've reached the max, then set the GameState to begin */
		if (players.size() == max)
			state = GameState.SETUP;
	}
	
	public void updatePlayer (Player player) {
		players.remove(player);
		players.add(player);
	}

	/**
	 * @return the grid
	 */
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

	public Dimension getSize() {
		return new Dimension (rows, columns);
	}
	
	@Column (name="nRows")
	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	@Column (name="nColumns")
	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public Set<Date> getMessageTimes () {
		return this.messages.keySet();
	}

	public String getMessage (Date date) {
		return this.messages.get(date);
	}

	public void addMessage(String message) {
		Date now = new Date (System.currentTimeMillis());
		this.messages.put(now, message);
	}

	public void setDimension(Dimension dimension) {
		 this.rows = (int) dimension.getHeight();
		 this.columns = (int) dimension.getWidth();
	}
	
	public int getMaxPlayers () {	
		int ret = 0;
		
		switch (type) {
			case CHECKERS:
				ret = 2;
				break;
			case PENTE:
				ret = 4;
				break;
		}
		
		return ret;
	}
	
	public void initialize (GameType type) {
		setType(type);
		setGrid(new GameGrid());
		setState(GameState.WAITING);
		switch (type) {
			case CHECKERS:
				setDimension (new Dimension (8,8));
				break;
			case PENTE:
				setDimension (new Dimension (20, 20));
				break;
			default:
				setDimension (new Dimension (0,0));
		}
	}

	public void removePlayer(Player player) {
		this.players.remove(player);
	}
}
