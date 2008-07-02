/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import java.util.Comparator;

import mx.ecosur.multigame.ejb.entity.Game;

import org.apache.commons.collections.buffer.PriorityBuffer;



/**
 * @author awater
 *
 */
@Entity
@DiscriminatorValue("PENTE")
public class PenteGame extends Game {
	
	private PriorityBuffer winners;
	
	public PriorityBuffer getWinners () {
		if (winners == null) {
			winners = determineWinners ();
		}
		
		return winners;
	}
	
	public void setWinners (PriorityBuffer queue) {
		this.winners = queue;
	}
	
	private PriorityBuffer determineWinners() {
		PriorityBuffer ret = new PriorityBuffer (new PlayerComparator ());
		
		
		return ret;
	}

	class PlayerComparator implements Comparator<PentePlayer> {

		public int compare(PentePlayer p1, PentePlayer p2) {
			int ret = 0;
		
			if (p1.getPoints() >p2.getPoints())
				ret = 1;
			else if (p1.getPoints() < p2.getPoints())
				ret = -1;
			
			return ret;
		}
	}
}
