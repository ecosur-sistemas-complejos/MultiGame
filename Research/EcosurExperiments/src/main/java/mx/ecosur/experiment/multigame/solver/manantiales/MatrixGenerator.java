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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.grid.util.Permutations;

/**
 *  Genreates all Matrices of interest to the solver.  Basically, a port of 
 *  Luis's rompecabezas program from Pascal. 
 *  
 *  @author lgarcia@ecosur.mx
 *  @author awaterma@ecosur.mx
 */

public class MatrixGenerator {
	
	public Set<Matrix> find () {
		Set<Matrix> ret = new HashSet<Matrix>();
		Set<Distribution> premiums = findDistributions(true);
		Set<Distribution> viables = findDistributions(false);
		for (int i = 1; i < 4; i++) 
			ret.addAll(testCombinationsAndPermutations(premiums, viables, i));
		return ret;
	}

	protected Set<Distribution> findDistributions (boolean withPremiums) {
		Set<Distribution> ret = new HashSet<Distribution>();
		for (int i = 0; i < 6; i++) {
			for (int b = 0;  b < 12; b++) {
				for (int m = boundingValue(24, b, i); 
					m < boundingValue (26, b, i); m++) 
				{
					Distribution distribution = new Distribution (
							Color.UNKNOWN, b, m, i);
					if (isViable (withPremiums, distribution)) {
						ret.add(distribution);
					}
				}
			}
		}
		
		return ret;
	}
	
	protected Set<Matrix> testCombinationsAndPermutations (Set<Distribution> premiums, 
			Set<Distribution> viables, int premiumsToTest)
	{
		Set<Matrix> ret = new HashSet<Matrix>();
		Color [] colors = { Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED };
		List<Distribution> distributions = new ArrayList<Distribution>();
		
		
			/* Set the required number of premiums */
		for (Distribution premium : premiums) {
			distributions.add (premium);
			if (distributions.size() < premiumsToTest)
				continue;
			else
				break;
		}
		
		List<Distribution> outer = new ArrayList<Distribution>();
		outer.addAll(distributions);
		Distribution[] viableArray = viables.toArray(new Distribution [ viables.size() ]);
		
		for (int i = 0; i < viableArray.length; i++) {
			Distribution viable = viableArray [ i ];
			for (int j = 0; j < 3; j++) {
				if (i == 0) {
					distributions = fill (distributions, viable, 3);
				} else if (i == 1) {
					distributions = fill (distributions, viable, 2);
				} else if (i == 2) {
					distributions = fill (distributions, viable, 1);
				}
				
				/* Fill in the remaining space in the matrix */
				for (int k = 1; k < 4 ; k++) {
					if (distributions.size() < 4 && k + i < viableArray.length)
						distributions.add (viableArray [ i + k ]);
					else
						break;
				}
				
				/* Check all permutations of this combination */
				Permutations perms = new Permutations(distributions.size());
				int [] indices;
				
				List<Distribution> matrix = new ArrayList<Distribution>();
				
				/* All permutations of this combination are posed against the
				 * matrix validity tests 
				 */
				while (perms.hasMore()) {
					indices = perms.getNext();
					for (int l = 0; l < indices.length; l++) {
						if (distributions.get(indices [ l ]) != null) {
							Distribution dist = new Distribution (distributions.get(indices [ l ]));
							/* Color the distribution */
							dist.setColor(colors [ l ] );
							matrix.add (dist);
							if (matrix.size() == 4) {
								Matrix test = new Matrix (matrix);
								if (isViable (test)) {
									ret.add(test);
								}
							
								matrix = new ArrayList<Distribution> ();
							}
						}
					}
				}
				
				distributions = new ArrayList<Distribution>();
				distributions.addAll(outer);
			}
		}
		
		return ret;
	}
	
	/**
	 * @param testMatrix
	 * @param viable
	 * @param i
	 */
	protected List<Distribution> fill (List<Distribution> distList,  
			Distribution viable, int times) 
	{
		for (int i = 0; i < times; i++) {
			if (distList.size() < 4)
				distList.add (viable);
		}
		return distList;
	}

	public boolean isViable(boolean withPremiums, Distribution distribution) {
		boolean ret = false;
		
		if (hasViableScore(23, 27, distribution)) {
			ret = true;
		}
		
		if (withPremiums)
			ret = ret && hasPremiums (6, distribution);

		return ret;
	}
	
	/* Distribution validation */
	
	protected boolean hasViableScore(int lower, int upper, Distribution dist) {
		if (!dist.viableScore(lower, upper))
			return false;
		else
			return true;
	}		
	
	protected boolean hasViableNumberOfIntensives(Matrix matrix) {
		boolean ret = false;
		int totalIntensives = 0;
		List<Distribution> distributions = matrix.getDistributions();
		for (Distribution dist : distributions) {
			totalIntensives += dist.getIntensive();
		}

		if (totalIntensives <= 16)
			ret = true;
		return ret;
	}
	
	protected boolean hasPremiums(int premium, Distribution dist) {
		return (dist.getIntensive() == premium || dist.getForest() == premium);
	}	
	
	
	/* Matrix validation */

	public boolean isViable(Matrix matrix) {
		boolean ret = true;
		ret = ret && hasValidColoring(matrix);
		ret = ret && hasViableIntensiveRatio(matrix);
		ret = ret && hasViableNumberOfIntensives(matrix);
		ret = ret && isSymetric(matrix);
		ret = ret && hasPremiums(matrix);
		ret = ret &&  matrix.getCount() <= 48; 
		return ret;
	}
	
	protected boolean hasValidColoring (Matrix matrix) {
		Set<Color> colorSet = new HashSet<Color>();
		for (Distribution dist : matrix.getDistributions()) {
			colorSet.add(dist.getColor());
		}
		
		return colorSet.size() == 4;
	}
	
	
	
	/**
	 * @param matrix
	 * @return
	 */
	protected boolean hasPremiums(Matrix matrix) {
		boolean ret = false;
		for (Distribution dist : matrix.getDistributions()) {
			if (dist.getIntensive() == 6 || dist.getForest() == 6) {
				ret = true;
				break;
			}	
		}
		
		return ret;
	}

	protected boolean hasViableIntensiveRatio(Matrix matrix) {
		boolean ret = true;
		Distribution blue = matrix.getDistribution(Color.BLUE),
			green = matrix.getDistribution(Color.GREEN), 
			yellow = matrix.getDistribution(Color.YELLOW), 
			red = matrix.getDistribution(Color.RED);
		if (blue == null || green == null || red == null || yellow == null)
			throw new RuntimeException ("Null distributions in matrix! [" + 
					matrix.toString() + "]");
		ret = ret && (blue.getIntensive() + green.getIntensive() <= 10);
		ret = ret && (green.getIntensive() + red.getIntensive() <= 10);
		ret = ret && (red.getIntensive() + yellow.getIntensive() <= 10);
		return ret && (blue.getIntensive() + yellow.getIntensive() <= 10);
	}
	
	protected boolean isSymetric(Matrix matrix) {
		boolean ret = true;
		Distribution blue = matrix.getDistribution(Color.BLUE), 
			green = matrix.getDistribution(Color.GREEN), 
			yellow = matrix.getDistribution(Color.YELLOW), 
			red = matrix.getDistribution(Color.RED);
		ret = ret && (blue.getCount() + green.getCount() >= 22);
		ret = ret && (blue.getCount() + green.getCount() <= 24);
		ret = ret && (blue.getCount() >= 10);
		ret = ret && (red.getCount() + yellow.getCount() >= 22);
		ret = ret && (red.getCount() + yellow.getCount () <= 24);
		ret = ret && (green.getCount() >= 10);
		return ret;
	}
	
	
	/** Utility methods **/

	protected int boundingValue (int points, int forest, int intensive) {
		return Math.round(
					(points - (forest * TokenType.MANAGED_FOREST.value()) - 
					intensive * TokenType.INTENSIVE_PASTURE.value()) / 
						TokenType.MODERATE_PASTURE.value());
	}
}
