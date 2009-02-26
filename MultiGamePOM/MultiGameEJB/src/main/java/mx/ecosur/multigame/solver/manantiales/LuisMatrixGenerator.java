/*
*Cpyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.solver.manantiales;

import java.util.HashSet;
import java.util.Set;

import mx.ecosur.multigame.Color;

public class LuisMatrixGenerator extends MatrixGenerator {
	
	@Override
	public Set<Matrix> find () {
		Set<Matrix> ret = new HashSet<Matrix>();
			/* Generate all possible matrices and test viability */
		for (int i1 = 0; i1 < 6; i1++) {
		  for (int b1 = 0; b1 <6; b1++) {
			for (int m1 = boundingValue (24, b1, i1); m1 < 
				boundingValue (26, b1, i1); m1++) {
			  for (int i2 = 0; i2 < 6; i2++) {
				for (int b2 = 0; b2 <6; b2++) {
				  for (int m2 = boundingValue (24, b2, i2); m2 < 
				  	boundingValue (26, b2, i2); m2++) {
					for (int i3 = 0; i3 < 6; i3++) {  
			          for (int b3 = 0; b3 <6; b3++ ) {
					    for (int m3 = boundingValue (24, b3, i3); m3 < 
					    	boundingValue (26, b3, i3); m3++) {
					      for (int i4 = 0; i4 < 6; i4++) {	
				            for (int b4 = 0; b4 <6; b4++) {
					          for (int m4 = boundingValue (24, b4, i4); m4 < 
					          	boundingValue (26, b4, i4); m4++) 
					          {
					        	Matrix test = new Matrix (
					        	  new Distribution (Color.BLUE,b1,m1,i1),
					        	  new Distribution (Color.GREEN,b2,m2,i2),
					        	  new Distribution (Color.YELLOW,b3,m3,i3),
					        	  new Distribution (Color.RED,b4,m4,i4));
					        	if (isViable(test))
					        	  ret.add(test); }}}}}}}}}}}}
		return ret;
	}
}
