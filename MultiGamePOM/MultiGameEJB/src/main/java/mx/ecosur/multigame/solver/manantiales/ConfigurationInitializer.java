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

import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.solution.initializer.StartingSolutionInitializer;

public class ConfigurationInitializer implements StartingSolutionInitializer {

	/* (non-Javadoc)
	 * @see org.drools.solver.core.solution.initializer.StartingSolutionInitializer#initializeSolution(org.drools.solver.core.localsearch.LocalSearchSolverScope)
	 */
	public void initializeSolution(LocalSearchSolverScope localSearchSolverScope) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.drools.solver.core.solution.initializer.StartingSolutionInitializer#isSolutionInitialized(org.drools.solver.core.localsearch.LocalSearchSolverScope)
	 */
	public boolean isSolutionInitialized(
			LocalSearchSolverScope localSearchSolverScope) {
		// TODO Auto-generated method stub
		return false;
	}

}
