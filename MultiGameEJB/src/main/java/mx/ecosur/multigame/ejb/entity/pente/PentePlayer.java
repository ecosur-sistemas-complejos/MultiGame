/**
 * 
 */
package mx.ecosur.multigame.ejb.entity.pente;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.pente.BeadString;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Player;

import java.util.HashSet;
import java.util.List;

/**
 * @author awater
 *
 */

@Entity
public class PentePlayer extends GamePlayer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7540174337729169503L;

	private int points;
	
	private HashSet<BeadString> trias, tesseras;
	
	private PentePlayer partner;
	
	public PentePlayer () {
		super ();
	}
	
	public PentePlayer(Game game, Player player, Color favoriteColor) {
		super (game, player, favoriteColor);
		points = 0;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}
	
	public HashSet<BeadString> getTrias() {
		if (trias == null)
			trias = new HashSet<BeadString>();
		return trias;
	}

	public void setTrias(HashSet<BeadString> trias) {
		this.trias = trias;
	}
	
	public void addTria (BeadString tria) {
		if (trias == null) {
			trias = new HashSet<BeadString> ();
		}
		
		if (tria.size() == 3)
			trias.add (tria);
	}

	public HashSet<BeadString> getTesseras() {
		if (tesseras == null) 
			tesseras = new HashSet<BeadString>();
		return tesseras;
	}

	public void setTesseras(HashSet<BeadString> tesseras) {
		this.tesseras = tesseras;
	}
	
	public void addTessera (BeadString tessera) {
		if (tesseras == null) {
			tesseras = new HashSet<BeadString> ();
		}
		
		if (tessera.size() == 4)
			tesseras.add(tessera);
	}
	
	@OneToOne (cascade={CascadeType.ALL})
	public PentePlayer getPartner() {
        if (partner == null) {
            Color color = this.getColor().getCompliment();
            List<GamePlayer> players = getGame().getPlayers();
            for (GamePlayer p : players) {
                if (p.getColor() != color)
                    continue;
                partner = (PentePlayer) p;
                break;
            }
        }
        return partner;
    }

    public void setPartner(PentePlayer partner) {
        this.partner = partner;
    }
	
	public boolean containsString (BeadString comparison) {
		for (BeadString string : getTrias()) {
			if (comparison.contains(string))
				return true;
		}
		
		for (BeadString string : getTesseras()) {
			if (comparison.contains(string))
				return true;
		}
		return false;
	}
}
