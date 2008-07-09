/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import javax.persistence.Entity;

import mx.ecosur.multigame.ejb.entity.GamePlayer;

/**
 * @author awater
 *
 */

@Entity
public class PentePlayer extends GamePlayer {
	
	private int points;

	
	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}	

}
