/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.pasale;

import javax.persistence.Entity;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.grid.entity.GridRegistrant;

@Entity
public class PasalePlayer extends GridPlayer {
	
	private static final long serialVersionUID = 2815240265995571202L;
	
	private int points;
	
	public PasalePlayer() {
		super();
	}
	
	public PasalePlayer(GridRegistrant player, Color favoriteColor) {
		super (player, favoriteColor);
		points = 0;
	}

    public int getScore() {
		return points;
	}
	
	public void setScore (int score) {
		this.points = score;
	}

	public void reset() {
		this.points = 0;
	}
}
