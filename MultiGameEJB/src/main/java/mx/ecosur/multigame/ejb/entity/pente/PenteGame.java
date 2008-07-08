/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.Game;



/**
 * @author awater
 *
 */
@NamedQueries ({
	@NamedQuery(name="getPenteGame",
			query="select g from PenteGame g where g.type=:type " +
				"and g.state <>:state"),
	@NamedQuery(name="getPenteGameById",
			query="select g from PenteGame g where g.id=:id " +
				"and g.state<>:state")})
@Entity
public class PenteGame extends Game {
	
	private Set<PentePlayer> winners;
	
	@ManyToMany (fetch=FetchType.EAGER)
	public Set <PentePlayer> getWinners () {
		if (winners == null) {
			winners = determineWinners ();
		}
		
		return winners;
	}
	
	public void setWinners (Set<PentePlayer> winners) {
		this.winners = winners;
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
