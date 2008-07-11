/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

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
	
	@Transient
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
		return ret;
	}
	
	
	class PlayerComparator implements Serializable, Comparator <PentePlayer>{

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
