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

import java.util.ArrayList;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridMove;

import mx.ecosur.multigame.impl.model.gente.BeadString;

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
	
	private ArrayList<Color> teamColors;
	
	private int searchCount;
	
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
	
	public void addTria(BeadString t) {
		boolean contained = false;
		
		if (trias == null)
			trias = new HashSet<BeadString> ();
		/* Only add trias that are not contained by other trias */
		for (BeadString tria : trias) {
			if (tria.contains(t)) {
				contained = true;
				break;
			}
				
		}
		
		if (!contained)
			trias.add(t);
	}

	/**
	 * Gets the Trias that this move created.
	 */
	public HashSet<BeadString> getTrias () {
		if (trias == null)
			trias = new HashSet<BeadString> ();		
		return trias;
	}
	
	public void setTrias (HashSet<BeadString> new_trias) {
		trias = new_trias;
	}
	
	public void addTessera (BeadString t) {
		boolean contained = false;
		
		if (tesseras == null)
			tesseras = new HashSet<BeadString> ();
		for (BeadString tessera : tesseras) {
			if (tessera.contains(t)) {
				contained = true;
				break;
			}
				
		}
		
		if (!contained)
			tesseras.add(t);
	}
	
	/**
	 * Gets the Tesseras that this move created.  
	 */
	public HashSet<BeadString> getTesseras () {	
		if (tesseras == null)
			tesseras = new HashSet<BeadString> ();		
		return tesseras;
	}
	
	public void setTesseras (HashSet<BeadString> new_tesseras) {
		tesseras = new_tesseras;
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


	/**
	 * @return the teamColors
	 */
	public ArrayList<Color> getTeamColors() {
		if (teamColors == null) {
			teamColors = new ArrayList<Color>();
			GentePlayer player = (GentePlayer) this.player;
			teamColors.add(this.player.getColor());
			teamColors.add (player.getPartner().getColor());
		}
		
		return teamColors;
	}


	/**
	 * @param teamColors the teamColors to set
	 */
	public void setTeamColors(ArrayList<Color> teamColors) {
		this.teamColors = teamColors;
	}


	/**
	 * @return the searchCount
	 */
	public int getSearchCount() {
		return searchCount;
	}


	/**
	 * @param searchCount the searchCount to set
	 */
	public void setSearchCount(int searchCount) {
		this.searchCount = searchCount;
	}


	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridMove#toString()
	 */
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append("GenteMove: ");
		ret.append("current=" + getCurrent() + ", ");		
		ret.append("destination=" + getDestination() + ", ");
		ret.append("qualifier=" + getQualifier() + ", ");
		ret.append("trias=" + getTrias().size() + ", ");
		ret.append("tesseras=" + getTesseras().size() + ", ");
		ret.append("teamColors=" + getTeamColors());
		
		return ret.toString();
	}


	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridMove#getDestination()
	 */
	@Override
	public GridCell getDestination() {
		// TODO Auto-generated method stub
		return super.getDestination();
	}


	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridMove#setDestination(mx.ecosur.multigame.impl.model.GridCell)
	 */
	@Override
	public void setDestination(GridCell destination) {
		// TODO Auto-generated method stub
		super.setDestination(destination);
	}
}
