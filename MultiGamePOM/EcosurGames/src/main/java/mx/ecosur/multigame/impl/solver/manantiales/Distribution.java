/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.solver.manantiales;

import mx.ecosur.multigame.Color;

public class Distribution {
	
	Color color;
	
	int forest, moderate, intensive, silvopastoral;
	
	public Distribution (Distribution dist) {
		this.color = dist.getColor();
		this.forest = dist.getForest();
		this.moderate = dist.getModerate();
		this.intensive = dist.getIntensive();
		this.silvopastoral = dist.getSilvopastoral();
	}
	
	public Distribution (Color color, int forest, int moderate, int intensive) {
		this (color, forest, moderate, intensive, 0);
	}
	
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
	
	public void setColor (Color color) {
		this.color = color;
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
	
	public int getScore () {
		return forest + moderate * 2 + intensive * 3 + silvopastoral * 4;
	}
	
	public int getCount () {
		return forest + moderate + intensive + silvopastoral;
	}
	
	public boolean viableScore (int lowerBound, int upperBound) {
		return (getScore () > lowerBound && getScore () < upperBound);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return " F: " + this.getForest() + ", M: " + 
			this.getModerate() + ", I: " + this.getIntensive() + ", S: " + 
			this.getSilvopastoral() + " == " + this.getScore() + "\n";
	}
}
