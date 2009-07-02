/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * GenteMove extends Move to add some Pente specific methods for use by the 
 * Pente/Gente rules.  
 * 
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.gente;

import java.util.HashMap;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridGame;
import mx.ecosur.multigame.impl.model.GridMove;

import mx.ecosur.multigame.impl.model.gente.BeadString;

import mx.ecosur.multigame.impl.util.Search;
import mx.ecosur.multigame.impl.util.Vertice;
import mx.ecosur.multigame.model.implementation.GamePlayerImpl;


@Entity
@NamedQueries( { 
	@NamedQuery(name = "getPenteMoves", 
			query = "select pm from GenteMove pm where pm.player.game=:game order by pm.id asc") 
})
public class GenteMove extends GridMove {
	
	private static final long serialVersionUID = -6635578671376146204L;

	public enum CooperationQualifier {
		COOPERATIVE, SELFISH, NEUTRAL
	}
	
	private HashSet<BeadString> trias, tesseras;

	private CooperationQualifier qualifier;
	
	private Search searchUtil;
	
	public GenteMove () {
		super ();
	}
	
	
	
	/**
	 * @param genteStrategyAgent
	 * @param cell
	 */
	public GenteMove(GridPlayer player, GridCell cell) {
		super (player, cell);
	}


	/**
	 * Gets the Trias that this move created.
	 */
	public HashSet<BeadString> getTrias () {
		if (searchUtil == null) {
			GridGame game = (GridGame) getPlayer().getGame();			
			searchUtil = new Search(game.getGrid());
		}
		if (trias == null) {
			trias = new HashSet<BeadString> ();
			HashMap<Vertice, HashSet<BeadString>> stringMap = 
				searchUtil.getString ((GridCell) getDestination(), 3);
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
		if (searchUtil == null) {
			GridGame game = (GridGame) getPlayer().getGame();		
			searchUtil = new Search(game.getGrid());
		}
		if (tesseras == null) {
			tesseras = new HashSet<BeadString>();
			HashMap<Vertice, HashSet<BeadString>> stringMap = 
				searchUtil.getString ((GridCell) getDestination(), 4, true);
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
	
	/* Tests as to whether the Registrant or his Partner, already contains a reference 
	 * to the candidate string. 
	 */
	private boolean uncountedString(BeadString string) {
		boolean ret = false;		
		GentePlayer pentePlayer = (GentePlayer) getPlayer();
		GentePlayer partner = pentePlayer.getPartner();
		if (!pentePlayer.containsString(string) && !partner.containsString(string))
			ret = true;
		return ret;
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
		
		if (obj instanceof GenteMove) {
			GenteMove comparator = (GenteMove) obj;
			if (comparator.player == this.player 
					&& comparator.getDestination() == this.getDestination() 
					&& comparator.getCurrent() == this.getCurrent())
				ret = true;
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.implementation.MoveImpl#setPlayer(mx.ecosur.multigame.model.implementation.AgentImpl)
	 */
	public void setPlayer(GamePlayerImpl player) {
		this.player = (GridPlayer) player;
	}
}
