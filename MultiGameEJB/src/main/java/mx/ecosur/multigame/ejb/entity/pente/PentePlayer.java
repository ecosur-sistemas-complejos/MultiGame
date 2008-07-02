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
	
	private int points;

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}	

}
