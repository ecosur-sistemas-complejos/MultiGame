/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.Game;



/**
 * @author awater
 *
 */
@Entity
@DiscriminatorValue("PENTE")
public class PenteGame extends Game {
	
	private TreeSet<PentePlayer> winners;
	
	public Set <PentePlayer> getWinners () {
		if (winners == null) {
			winners = determineWinners ();
		}
		
		return winners;
	}
	
	private TreeSet<PentePlayer> determineWinners() {
		TreeSet<PentePlayer> ret = new TreeSet<PentePlayer> (new PlayerComparator());
		for (Player p : getPlayers()) {
			PentePlayer player = new PentePlayer(p);
			ret.add(player);
		}
		
		return ret;
	}
	
	
	class PlayerComparator implements Comparator <PentePlayer>{

		public int compare(PentePlayer alice, PentePlayer bob) {
			int ret = 0;
			
			PentePlayer p1 = (PentePlayer) alice, p2 = (PentePlayer) bob;
			if (p1.getPoints() > p2.getPoints())
				ret = 1;
			else if (p1.getPoints() < p2.getPoints())
				ret = -1;
			return ret;	
		}
	}
}
