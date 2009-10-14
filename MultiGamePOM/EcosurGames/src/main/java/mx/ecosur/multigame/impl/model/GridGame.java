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

package mx.ecosur.multigame.impl.model;

import java.awt.Dimension;
import java.util.*;

import javax.persistence.*;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.model.Agent;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.implementation.AgentImpl;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.GamePlayerImpl;
import mx.ecosur.multigame.model.implementation.Implementation;
import mx.ecosur.multigame.model.implementation.MoveImpl;
import org.drools.agent.KnowledgeAgent;
import org.drools.KnowledgeBase;

/**
 * @author awaterma, maxmil
 *
 */
@NamedQueries( {
	@NamedQuery(name = "getGameById", query = "select g from GridGame g where g.id=:id"),
	@NamedQuery(name = "getCurrentGames",
        query = "select DISTINCT gme from GridGame as gme, IN (gme.players) as players, " +
            "GridPlayer as player where player.registrant = :registrant and player MEMBER OF gme.players " +
            "and gme.state <> :state"),
    @NamedQuery(name = "getAvailableGames",
	    query = "select DISTINCT gme from GridGame as gme, IN (gme.players) as players, " +
            "GridPlayer as player where player.registrant = :registrant and gme.state = :state and player " +
                "NOT MEMBER OF gme.players")        
})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class GridGame implements GameImpl, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8785194013529881730L;

	/**
	 * Primary Key
	 */
	protected int id;
	
	/**
	 * The date the game was created
	 */
	protected Date created;
	
	/**
	 * The players involved in the game 
	 */
	protected List<GridPlayer> players;
	
	/**
	 * Moves from the game.
	 */
	protected Set<GridMove> moves;
	
	protected GameGrid grid;
	
	protected GameState state;

    protected transient KnowledgeAgent kagent;

    protected KnowledgeBase kbase;
	
	protected long version;
	
	/* Dimensioning for storage */
	private int rows, columns;
	
	public GridGame () {
		super();
		players = new ArrayList<GridPlayer> ();
		grid = new GameGrid();
	}

    public GridGame (KnowledgeAgent agent) {
        kagent = agent;
        players = new ArrayList<GridPlayer> ();
		grid = new GameGrid();	             
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
	
	@Transient
	public Set<Implementation> getFacts () {
		Set<Implementation> ret = new HashSet<Implementation>();
		ret.addAll(grid.getCells());
		return ret;
	}

	/**
	 * @return the players
	 */
	@OneToMany (cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
	public List<GridPlayer> getPlayers() {
		return players;
	}

	/**
	 * @param players the players to set
	 */
	public void setPlayers(List<GridPlayer> players) {
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
    @Transient
	public Dimension getSize() {
		return new Dimension (rows, columns);
	}
	
	public void updatePlayer (GridPlayer player) {
		players.remove(player);
		players.add(player);
	}
	
	public void removePlayer(GamePlayerImpl player) {
		this.players.remove(player);
	}

	/**
	 * @param query
	 * @return
	 */
	public Query setParameters(Query query) {
		query.setParameter("game", this);
		return query;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.GameImpl#getMoves()
	 */
    @Transient
	public Set<MoveImpl> listMoves() {
        Set<MoveImpl> ret = new LinkedHashSet<MoveImpl>();
		Set<GridMove> moves = getMoves();
        for (GridMove move : moves) {
            ret.add(move);
        }

		return ret;	

	}

    @OneToMany (cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
    public Set<GridMove> getMoves() {
        if (moves == null)
            moves = new LinkedHashSet<GridMove>();
        return moves;
    }

    public void setMoves (Set<GridMove> moves) {
        this.moves = moves;
    }

    @Transient
	protected List<Color> getAvailableColors() {
	    List<Color> colors = getColors();
	    for (GamePlayerImpl player : players) {
	    	GridPlayer ep = (GridPlayer) player;
	    	colors.remove(ep.getColor());
	    }
	 
	    return colors;
	}
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.GameImpl#listPlayers()
	 */
	public List<GamePlayer> listPlayers() {
		List<GamePlayer> ret = new ArrayList<GamePlayer>();
		for (GridPlayer player : players) {
			if (player instanceof AgentImpl)
				ret.add(new Agent((AgentImpl) player));
			else			
				ret.add(new GamePlayer (player));
		}
		
		return ret;
	}
	
	/* 
	 * Clones a copy of the implemented sub-class
	 */
    @Override
	protected abstract Object clone() throws CloneNotSupportedException;
	
    @Transient
	public abstract List<Color> getColors();

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.GameImpl#move(mx.ecosur.multigame.model.implementation.MoveImpl)
	 */
	public abstract MoveImpl move(MoveImpl move) throws InvalidMoveException;

}