/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.experiment.multigame.solver.manantiales;

import java.util.Arrays;
import java.util.List;

import mx.ecosur.multigame.impl.Color;

/**
 * A Matrix is a solvable set of distributions, such as what might appear
 * on a gameboard.  It takes 4 distributions to create a Matrix.
 */

public class Matrix {

	Distribution[] distributions;;
	
	public Matrix () {
		super ();
		distributions = new Distribution [ 4 ];
	}
	
	public Matrix (Matrix matrix) {
		this ();
		distributions [ 0 ] = matrix.getDistribution(Color.BLUE);
		distributions [ 1 ] = matrix.getDistribution(Color.GREEN);
		distributions [ 2 ] = matrix.getDistribution(Color.RED);
		distributions [ 3 ] = matrix.getDistribution(Color.YELLOW);
	}
	
	public Matrix (Distribution player1, Distribution player2, 
			Distribution player3, Distribution player4) 
	{
		this ();
		distributions [ 0 ] = player1;
		distributions [ 1 ] = player2;
		distributions [ 2 ] = player3;
		distributions [ 3 ] = player4;
	}
	
	public Matrix (List<Distribution> distList) {
		if (distList.size() > 4)
			throw new RuntimeException ("Matrices are composed of 4 " +
					"distributions!");
		distributions = distList.toArray(new Distribution [4]);
	}
	
	public List<Distribution> getDistributions() {
		return Arrays.asList(distributions);
	}
	
	public Distribution getDistribution (Color color) {
		Distribution ret = null;
		
		for (Distribution dist : distributions) {
			if (dist.getColor().equals(color)) { 
					ret = dist;
					break;
			}
		}
		
		return ret;
	}
	
	public Distribution getDistribution (int index) {
		return distributions [ index ];
	}
	
	public int getDeforestation () {
		int deforested = 0;
		for (Distribution dist : distributions) {
			deforested += dist.getIntensive() + dist.getModerate();
		}
		return deforested;
	}
	
	public int getCount () {
		int ret = 0;
		for (Distribution dist : distributions) {
			ret += dist.getCount();
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String ret = "";
		for (Color color : Color.values()) {
			if (color.equals(Color.UNKNOWN))
				continue;
			Distribution dist = getDistribution(color);
            if (dist != null) {
                ret += "[" + color + "]" + " F: " + dist.getForest() + ", M: " + 
                    dist.getModerate() + ", I: " + dist.getIntensive() + ", S: " + 
                    dist.getSilvopastoral() + " == " + dist.getScore() + "\n";
            }
		}
		
		return ret;
	}
}
