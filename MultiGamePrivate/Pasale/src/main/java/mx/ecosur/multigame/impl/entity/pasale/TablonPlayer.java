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

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;

@Entity
public class TablonPlayer extends GridPlayer {
	
	private static final long serialVersionUID = 2815240265995571202L;
	
	private int points, cheatYears;
	
	public TablonPlayer() {
		super();
	}
	
	public TablonPlayer(GridRegistrant player, Color favoriteColor) {
		super (player, favoriteColor);
		points = 0;
	}

    public int getCheatYears() {
        return cheatYears;
    }

    public void setCheatYears(int cheatYears) {
        this.cheatYears = cheatYears;
    }

    public int getScore() {
		return points;
	}
	
	public void setScore (int score) {
		this.points = score;
	}

	public void reset() {
		this.cheatYears = 0;
		this.points = 0;
	}
}
