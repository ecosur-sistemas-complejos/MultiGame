/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import mx.ecosur.multigame.ejb.entity.Player;

/**
 * @author awater
 *
 */
@Entity
@DiscriminatorValue("PENTE")
public class PentePlayer extends Player {
	
	public PentePlayer () {
		super ();
	}
	
	public PentePlayer (Player regular) {
		super ();
		this.setName(regular.getName());
		this.setColor(regular.getColor());
		this.setGamecount(regular.getGamecount());
		this.setId(regular.getId());
		this.setLastRegistration(regular.getLastRegistration());
		this.setTurn(regular.isTurn());
		this.setWins(regular.getWins());
		this.setPoints(0);
	}
	
	private int points;

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}	

}
