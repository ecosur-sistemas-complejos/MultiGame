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

package mx.ecosur.multigame.ejb.entity.pente;

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
import mx.ecosur.multigame.Direction;
import mx.ecosur.multigame.Vertice;
import mx.ecosur.multigame.pente.BeadString;
import mx.ecosur.multigame.util.Search;
import mx.ecosur.multigame.util.AnnotatedCell;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;


@Entity
@NamedQueries( { 
	@NamedQuery(name = "getPenteMoves", 
			query = "select pm from PenteMove pm where pm.player.game=:game order by pm.id asc") 
})
public class PenteMove extends Move {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6635578671376146204L;

	public enum CooperationQualifier {
		COOPERATIVE, SELFISH, NEUTRAL
	}

	private Set<Cell> captures;
	
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
	public Set<Cell> getCaptures () {
		if (searchUtil == null)
			searchUtil = new Search(getPlayer().getGame().getGrid());
		if (captures == null) {
			captures = new HashSet<Cell>();
			
			/* Find 2 levels of cells in all directions, from this move */
			Color [] candidateColors = getCandidateColors ();
			for (Color c : candidateColors) {
				Color [] searchColors = { c };
				Set<AnnotatedCell> candidates = searchUtil.findCandidates (
					new AnnotatedCell (getDestination()), getDestination(), 
						searchColors, 2);
			
				/* Directional Hash of cells */
				HashMap <Direction,ArrayList<Cell>> directionalMap = 
					new HashMap<Direction,ArrayList<Cell>>();
		
				/* Sort the candidates */
				for (AnnotatedCell candidate : candidates) {
					if (directionalMap.containsKey(candidate.getDirection())) {
						ArrayList<Cell> lst = directionalMap.get(candidate.getDirection());
						lst.add(candidate.getCell());
						directionalMap.put (candidate.getDirection(), lst);
					} else {
						ArrayList<Cell> lst = new ArrayList<Cell> ();
						lst.add(candidate.getCell());
						directionalMap.put(candidate.getDirection(), lst);
					}
				}
			
				/* For each Direction, check and see if the last cell in the list
				 * is bounded by a cell of this color 
				 */
				Set<Direction> keys = directionalMap.keySet();
				for (Direction direction : keys) {
					ArrayList<Cell> trapped = directionalMap.get(direction);
					if (trapped.size() != 2) 
						continue;
					Cell cell = trapped.get(1);
					Cell result = searchUtil.searchGrid (direction, cell, 
							getDestination().getColor(), 1);
					if (result != null) {
						captures.addAll (trapped);
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
		trias.addAll(new_trias);
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
		tesseras.addAll(new_tesseras);
	}
	
	/* Tests as to whether the Player already contains a reference to the 
	 * candidate string, or if any currently counted tesseras contain the
	 * string itself (in the case of a tria). 
	 */
	private boolean uncountedString(BeadString string) {
		boolean ret = false;
		if (string.size() == 3) {
			for (BeadString tessera : getTesseras()) {
				if (tessera.contains(string))
					return ret;
			}
		}
		
		PentePlayer pentePlayer = (PentePlayer) player;
		if (!pentePlayer.containsString(string))
			ret = true;
		return ret;
	}

	private Color[] getCandidateColors() {
		Color [] ret;
		
		List<GamePlayer> players = player.getGame().getPlayers();
		ArrayList<Color> colors = new ArrayList<Color>();
		ret = new Color [ players.size() -1 ];
		for (GamePlayer p : players) {
			if (p.equals(this.player))
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
}
