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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;



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
		return winners;
	}
	
	public void setWinners(Set<PentePlayer> winners){
		this.winners = winners;
	}
	
	public void determineWinners() {
		TreeSet<PentePlayer> ret = new TreeSet<PentePlayer> (new PlayerComparator());
		List <GamePlayer> players = this.getPlayers();
		for (GamePlayer p : players) {
			PentePlayer player = (PentePlayer) p;
			if (player.getPoints() > 0)
			ret.add(player);
		}
		
		winners = ret;
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
