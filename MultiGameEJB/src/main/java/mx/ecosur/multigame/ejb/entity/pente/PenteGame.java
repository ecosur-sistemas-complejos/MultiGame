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
	
	private TreeSet<Player> winners;
	
	public Set <Player> getWinners () {
		if (winners == null) {
			winners = determineWinners ();
		}
		
		return winners;
	}
	
	private TreeSet<Player> determineWinners() {
		TreeSet<Player> ret = new TreeSet<Player> (new PlayerComparator());
		for (Player p : getPlayers()) {
			PentePlayer player = (PentePlayer) p;
			ret.add(player);
		}
		
		return ret;
	}
	
	
	class PlayerComparator implements Comparator <Player>{

		public int compare(Player alice, Player bob) {
			int ret = 0;
			
			if (alice instanceof PentePlayer && bob instanceof PentePlayer) {
				PentePlayer p1 = (PentePlayer) alice, p2 = (PentePlayer) bob;
				if (p1.getPoints() > p2.getPoints())
					ret = 1;
				else if (p1.getPoints() < p2.getPoints())
					ret = -1;
			}
			return ret;	
		}
	}
}
