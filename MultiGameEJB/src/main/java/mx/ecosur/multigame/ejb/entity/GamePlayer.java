/**
 * 
 */
package mx.ecosur.multigame.ejb.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import mx.ecosur.multigame.Color;

/**
 * A GamePlayer contains persistent information about a player playing a 
 * specific game.  
 * 
 * @author awater
 *
 */
@NamedQueries ({
	@NamedQuery(name="getGamePlayer",
			query="select gp from GamePlayer gp where gp.player=:player " +
					"and gp.game=:game and gp.color=:color")})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class GamePlayer implements Serializable {
	
	private int id;
	
	private Player player;
	
	private Game game;

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

	@ManyToOne
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

}
