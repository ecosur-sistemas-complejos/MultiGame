/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * A GamePlayer contains persistent information about a player playing a 
 * specific game.  
 * 
 * @author awaterma@ecosur.mx
 *
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import mx.ecosur.multigame.Agent;
import mx.ecosur.multigame.Color;


@NamedQueries ({
	@NamedQuery(name="getGamePlayer",
			query="select gp from GamePlayer gp where gp.player=:player " +
					"and gp.game=:game")})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class GamePlayer implements Agent, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1893870933080422147L;

	private int id;
	
	private Player player;
	
	protected Game game;

	private Color color;
	
	private boolean turn;
	
	public static String getNamedQuery () {
		return "getGamePlayer";
	}

	public GamePlayer () {
		super();
	}
	
	public GamePlayer (Game game, Player player, Color color) {
		this.game = game;
		this.player = player;
		this.color = color;
	}
	
	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	@ManyToOne (cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
	@JoinColumn(name="GAME_ID")
	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	@Enumerated (EnumType.STRING)
	public Color getColor() {
		return color;
	}
	
	public void setColor (Color color) {
		this.color = color;
	}

	public boolean isTurn() {
		return turn;
	}

	public void setTurn(boolean turn) {
		this.turn = turn;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return player.getName() + ", " + color.name() + ", turn=" + turn;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.Agent#initialize(mx.ecosur.multigame.model.Game, mx.ecosur.multigame.model.Player, mx.ecosur.multigame.Color)
	 */
	public void initialize(Game game, Player registrant, Color favoriteColor) {
		this.game = game;
		this.player = registrant;
		this.color = favoriteColor;
	}

}
