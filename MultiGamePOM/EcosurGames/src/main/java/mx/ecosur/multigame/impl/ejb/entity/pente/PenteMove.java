/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * PenteMove extends Move to add some Pente specific methods for use by the 
 * Pente/Gente rules.  
 * 
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.ejb.entity.pente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.impl.pente.BeadString;
import mx.ecosur.multigame.impl.util.Direction;
import mx.ecosur.multigame.impl.util.Search;
import mx.ecosur.multigame.impl.util.Vertice;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;


@Entity
@NamedQueries( { 
	@NamedQuery(name = "getPenteMoves", 
			query = "select pm from PenteMove pm where pm.player.game=:game order by pm.id asc") 
})
public class PenteMove extends Move {
	
	private static final long serialVersionUID = -6635578671376146204L;

	public enum CooperationQualifier {
		COOPERATIVE, SELFISH, NEUTRAL
	}

	private HashSet<BeadString> captures;
	
	private HashSet<BeadString> trias, tesseras;

	private CooperationQualifier qualifier;
	
	private Search searchUtil;
	
	public PenteMove () {
		super ();
	}

	public PenteMove(PentePlayer player, Cell destination) {
		super(player, destination);
	}
	
	/*
	 * Returns a set of captured pieces, computed once. 
	 */
	@Transient
	public Set<BeadString> getCaptures () {
		if (captures == null) {
			if (searchUtil == null)
				searchUtil = new Search(getPlayer().getGame().getGrid());
			captures = new HashSet<BeadString>();
			
			/* Looking for all other colors  */
			Color [] candidateColors = getCandidateColors ();
			for (int i = 0; i < candidateColors.length; i++) {
				Cell fakeStarter = new Cell (getDestination().getColumn(),
					getDestination().getRow(), candidateColors [ i ]);
				HashMap<Vertice, HashSet<BeadString>> possible = searchUtil.getString(
						fakeStarter, 3);
				for (Vertice v : possible.keySet()) {
					HashSet<BeadString> strings = possible.get(v);
					for (BeadString string : strings) {
						/* Discover the direction this string is on */
						Direction direction = string.getDirection();
						Cell cell = string.getBeads().first().equals(
								fakeStarter) ? string.getBeads().last() : string.getBeads().first();
						Cell terminus = searchUtil.searchGrid(direction, cell, 1);
						if (terminus != null && terminus.getColor().equals(
								player.getColor())) {
							if (string.remove(fakeStarter))
								captures.add(string);
						}
					}
				}
			}
		}
		
		return captures;
	}
	
	/**
	 * Gets the Trias that this move created.
	 */
	public HashSet<BeadString> getTrias () {
		if (searchUtil == null)
			searchUtil = new Search(getPlayer().getGame().getGrid());
		if (trias == null) {
			trias = new HashSet<BeadString> ();
			HashMap<Vertice, HashSet<BeadString>> stringMap = 
				searchUtil.getString (getDestination(), 3);
			for (Vertice v: stringMap.keySet()) {
				HashSet<BeadString> stringSet = stringMap.get(v);
				for (BeadString string : stringSet) {
					if (uncountedString (string) && string.contiguous(v))
						trias.add(string);
				}
			}
		}
		
		return trias;
	}
	
	public void setTrias (HashSet<BeadString> new_trias) {
		trias = new_trias;
	}
	
	/**
	 * Gets the Tesseras that this move created.  This search will include
	 * this color's compliment.
	 * 
	 * NOTE:  This call works with the GENTE rules.  Eligible candidate for
	 * refactoring.
	 */
	public HashSet<BeadString> getTesseras () {
		if (searchUtil == null)
			searchUtil = new Search(getPlayer().getGame().getGrid());
		if (tesseras == null) {
			tesseras = new HashSet<BeadString>();
			HashMap<Vertice, HashSet<BeadString>> stringMap = 
				searchUtil.getString (getDestination(), 4, true);
			for (Vertice v : stringMap.keySet()) {
				HashSet<BeadString> stringSet = stringMap.get(v);
				for (BeadString string : stringSet) {
					if (uncountedString (string) && string.contiguous(v))
						tesseras.add(string);
				}
			}
		}
		
		return tesseras;
	}
	
	public void setTesseras (HashSet<BeadString> new_tesseras) {
		tesseras = new_tesseras;
	}
	
	/* Tests as to whether the Player or his Partner, already contains a reference 
	 * to the candidate string. 
	 */
	private boolean uncountedString(BeadString string) {
		boolean ret = false;		
		PentePlayer pentePlayer = (PentePlayer) player;
		PentePlayer partner = pentePlayer.getPartner();
		if (!pentePlayer.containsString(string) && !partner.containsString(string))
			ret = true;
		return ret;
	}

	private Color[] getCandidateColors() {
		Color [] ret;
		
		List<GamePlayer> players = player.getGame().getPlayers();
		ArrayList<Color> colors = new ArrayList<Color>();
		ret = new Color [ players.size() -1 ];
		PentePlayer pp = (PentePlayer) player;
		for (GamePlayer p : players) {
			if (p.equals(this.player) || p.equals(pp.getPartner()))
				continue;
			colors.add(p.getColor());
		}
		
		return colors.toArray(ret);
		
	}
	
	/**
	 * Gets the qualifier
	 * 
	 * @return the qualifier
	 */
	@Enumerated(EnumType.STRING)
	public CooperationQualifier getQualifier() {
		return qualifier;
	}

	/**
	 * Sets the cooperation qualifier
	 * 
	 * @param qualifier
	 *            the cooperation qualifier
	 */
	public void setQualifier(CooperationQualifier qualifier) {
		this.qualifier = qualifier;
	}

	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		
		if (obj instanceof PenteMove) {
			PenteMove comparator = (PenteMove) obj;
			if (comparator.player == this.player 
					&& comparator.getDestination() == this.getDestination() 
					&& comparator.getCurrent() == this.getCurrent())
				ret = true;
		}
		
		return ret;
	}
	
	
}
