/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.solver.manantiales;

import mx.ecosur.multigame.Color;

public class Distribution {
	
	Color color;
	
	int forest, moderate, intensive, silvopastoral;
	
	public Distribution (Color color, int forest, int moderate, int intensive,
		int silvopastoral) 
	{
		this.color = color;
		this.forest = forest;
		this.moderate = moderate;
		this.intensive = intensive;
		this.silvopastoral = silvopastoral;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return the forest
	 */
	public int getForest() {
		return forest;
	}

	/**
	 * @return the moderate
	 */
	public int getModerate() {
		return moderate;
	}

	/**
	 * @return the intensive
	 */
	public int getIntensive() {
		return intensive;
	}

	/**
	 * @return the silvopastoral
	 */
	public int getSilvopastoral() {
		return silvopastoral;
	}
}
