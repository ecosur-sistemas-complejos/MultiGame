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
import java.util.HashSet;
import java.util.Set;

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
			query="select g from PenteGame g where g.id=:id ") ,
	@NamedQuery(name="getPenteGamePlayer",
			query="select pgp from PentePlayer pgp where pgp.player=:player " +
					"and pgp.game=:game and pgp.color=:color")
})
@Entity
public class PenteGame extends Game {
	
	private Set<PentePlayer> winners;
	
	@OneToMany (fetch=FetchType.EAGER)
	public Set <PentePlayer> getWinners () {
		if (winners == null)
			winners = new HashSet<PentePlayer>();
		return winners;
	}
	
	public void setWinners(Set<PentePlayer> winners){
		this.winners = winners;
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
