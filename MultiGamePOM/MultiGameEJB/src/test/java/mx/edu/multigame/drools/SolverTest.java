/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.edu.multigame.drools;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.solver.manantiales.ManantialesSolution;
import mx.ecosur.multigame.solver.manantiales.SolutionConfigurer;

import org.drools.solver.config.XmlSolverConfigurer;
import org.drools.solver.core.Solver;
import org.drools.solver.core.solution.Solution;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

/**
 * This test ensures that the solver is configured and working correctly.
 * The intention is to use 
 */

public class SolverTest {
	
	private static Logger logger = Logger.getLogger(
			SolverTest.class.getCanonicalName());
	private static String configPath = 
		"/mx/ecosur/multigame/solver/manantiales-standard-solver.xml";
	private static String testModelPath = 
		"/mx/ecosur/multigame/solver/data/testSolution.xml";
	private static String tinyModelPath = 
		"/mx/ecosur/multigame/solver/data/tinySolution.xml";
	
	private XmlSolverConfigurer configurer;
	private Solution startingSolution;
	
	@Before
	public void setUp () {
		configurer = new XmlSolverConfigurer();
		configurer.configure(new InputStreamReader(this.getClass()
				.getResourceAsStream(configPath)));
		ManantialesGame game = new ManantialesGame();
		game.initialize(GameType.MANANTIALES);
		startingSolution = new ManantialesSolution(game.getTokens());
	}
	
	public void testSolver () throws JDOMException, IOException {
		SolutionConfigurer solcon = new SolutionConfigurer(
				(ManantialesSolution) startingSolution);
		Document doc = solcon.load(testModelPath);
		startingSolution = solcon.configure(doc);
		Solver solver = configurer.buildSolver();
		solver.setStartingSolution(startingSolution);
		ManantialesSolution solution = (ManantialesSolution) startingSolution;
		logger.info ("Invoking solver, this should take a few minutes...");
		logger.info ("Starting distribution: " + solution.getDistribution());
		solver.solve();
		solution = (ManantialesSolution) solver.getBestSolution();
		logger.info ("Solution found!  Best score = " + solver.getBestScore() + 
				" in " + solver.getTimeMillisSpend() + " ms");
		logger.info ("Final distribution " + solution.getDistribution());
	}
	
	@Test
	public void testSolverWithUndevelopedSolution () throws JDOMException, IOException {
		SolutionConfigurer solcon = new SolutionConfigurer(
				(ManantialesSolution) startingSolution);
		Document doc = solcon.load(tinyModelPath);
		startingSolution = solcon.configure(doc);
		Solver solver =configurer.buildSolver();
		solver.setStartingSolution(startingSolution);
		ManantialesSolution solution = (ManantialesSolution) startingSolution;
		logger.info ("Invoking solver, this should take a few minutes...");
		logger.info ("Starting distribution: " + solution.getDistribution());
		solver.solve();		
		solution = (ManantialesSolution) solver.getBestSolution();
		logger.info ("Best score = " + solver.getBestScore() + " in " + 
				solver.getTimeMillisSpend() + " ms");
		logger.info ("Final distribution " + solution.getDistribution());
	}
}
