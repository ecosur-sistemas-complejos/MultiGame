/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.manantiales.enums;

import java.util.ArrayList;
import java.util.List;

import mx.ecosur.multigame.grid.Color;

public enum BorderType {
	
	NORTH, EAST, SOUTH, WEST, NONE;
	
	/* Returns the colors */ 
	public List<Color> getColors () {
		List<Color> colors = new ArrayList<Color>();
		switch (this) {
			case NORTH:
				colors.add(Color.BLUE);
				colors.add(Color.GREEN);
				break;
			case EAST:
				colors.add(Color.GREEN);
				colors.add(Color.YELLOW);
				break;
			case SOUTH:
				colors.add(Color.YELLOW);
				colors.add(Color.RED);
				break;
			case WEST:
				colors.add(Color.RED);
				colors.add(Color.BLUE);
				break;
			default:
				break;
		}
		
		/* Unknown colored tokens are always compliments on the border  */
		colors.add(Color.UNKNOWN);
		
		return colors;
	}
}
