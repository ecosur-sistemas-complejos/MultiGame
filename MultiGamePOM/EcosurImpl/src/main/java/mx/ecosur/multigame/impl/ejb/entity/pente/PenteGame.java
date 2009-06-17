/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * PenteGame extends the general Game object with some Pente (or Gente) specific
 * methods and functionality.  PenteGame provides callers with the winners of 
 * the game it manages to.
 * 
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.ejb.entity.pente;

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
