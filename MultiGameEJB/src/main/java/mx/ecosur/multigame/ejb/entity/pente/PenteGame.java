/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.ejb.entity.Game;



/**
 * @author awater
 *
 */
@NamedQueries( {
	@NamedQuery(name = "getPenteGame", query = "select g from PenteGame g where g.type=:type "
		+ "and g.state =:state"),
	@NamedQuery(name = "getPenteGameById", query = "select g from PenteGame g where g.id=:id "),
	@NamedQuery(name = "getPenteGameByTypeAndPlayer", query = "select pp.game from PentePlayer as pp "
		+ "where pp.player=:player and pp.game.type=:type and pp.game.state <>:state") 
})
@Entity
public class PenteGame extends Game {
	
	private static final long serialVersionUID = -4437359200244786305L;
	
	private Set<PentePlayer> winners;
	
	@OneToMany (fetch=FetchType.EAGER)
	public Set <PentePlayer> getWinners () {
		if (winners == null)
			winners = new TreeSet<PentePlayer>(new PlayerComparator());
		return winners;
	}
	
	public void setWinners(Set<PentePlayer> winners){
		this.winners = winners;
	}
	
	/* Method included soleley for signature, issue with Glassfish Deployment */
	public void determineWinners() {
	}
	
	
	class PlayerComparator implements Serializable, Comparator <PentePlayer>{

		private static final long serialVersionUID = 8076875284327150645L;

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
