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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.ejb.entity.manantiales.Token;
import mx.ecosur.multigame.solver.manantiales.Distribution;
import mx.ecosur.multigame.solver.manantiales.ManantialesSolution;
import mx.ecosur.multigame.solver.manantiales.Matrix;
import mx.ecosur.multigame.solver.manantiales.MatrixGenerator;
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

public class StandardSolverTest {
	
	private static Logger logger = Logger.getLogger(
			StandardSolverTest.class.getCanonicalName());
	private static String configPath = 
		"/mx/ecosur/multigame/solver/manantiales-standard-solver.xml";
	private static String mutatePath = 
		"/mx/ecosur/multigame/solver/manantiales-mutate-solver.xml";
	private static String testModelPath = 
		"/mx/ecosur/multigame/solver/data/testSolution.xml";
	private static String symmetricPath = 
		"/mx/ecosur/multigame/solver/data/444-444-444-444-distribution.xml";
	private static String nonSymmetricPath = 
		"/mx/ecosur/multigame/solver/data/distribution.xml";
	
	private XmlSolverConfigurer configurer;
	private Solution startingSolution;
	
	@Before
	public void setUp () {
		configurer = new XmlSolverConfigurer();
		configurer.configure(new InputStreamReader(this.getClass()
				.getResourceAsStream(configPath)));
		ManantialesGame game = new ManantialesGame();
		game.initialize(GameType.MANANTIALES);
		startingSolution = new ManantialesSolution(
				ManantialesSolution.Threshold.SIMPLE, (SortedSet<Token>) 
				game.getTokens());
	}
	
	@Test
	public void testSolver () throws JDOMException, IOException {
		SolutionConfigurer solcon = new SolutionConfigurer(
				(ManantialesSolution) startingSolution);
		Document doc = solcon.load(testModelPath);
		startingSolution = solcon.configure(doc);
		Solver solver = configurer.buildSolver();
		solver.setStartingSolution(startingSolution);
		ManantialesSolution solution = (ManantialesSolution) startingSolution;
		logger.info ("Invoking solver, this should take a few seconds...");
		logger.info (getDistributions(solution));
		logger.info (solution.toString());
		solver.solve();
		solution = (ManantialesSolution) solver.getBestSolution();
		logger.info (solution.toString());
		logger.info (getDistributions(solution));
		assertEquals (0.0, solver.getBestScore());
	}
	
	@Test
	public void testDistribution () throws JDOMException, IOException {
		SolutionConfigurer solcon = new SolutionConfigurer(
				(ManantialesSolution) startingSolution);
		Document doc = solcon.load(symmetricPath);
		startingSolution = solcon.configure(doc);
		Solver solver =configurer.buildSolver();
		solver.setStartingSolution(startingSolution);
		ManantialesSolution solution = (ManantialesSolution) startingSolution;
		logger.info ("Invoking solver, this should take a few seconds...");
		logger.info (getDistributions(solution));
		logger.info (solution.toString());
		solver.solve();		
		solution = (ManantialesSolution) solver.getBestSolution();
		logger.info (solution.toString());
		assertEquals (0.0, solver.getBestScore());
	}
	
	public void testMutated () throws JDOMException, IOException {
		configurer = new XmlSolverConfigurer();
		configurer.configure(new InputStreamReader(this.getClass()
				.getResourceAsStream(mutatePath)));
		ManantialesGame game = new ManantialesGame();
		game.initialize(GameType.MANANTIALES);
		startingSolution = new ManantialesSolution(
				ManantialesSolution.Threshold.SIMPLE, (SortedSet<Token>) 
				game.getTokens());
		SolutionConfigurer solcon = new SolutionConfigurer(
				(ManantialesSolution) startingSolution);
		Document doc = solcon.load(nonSymmetricPath);
		startingSolution = solcon.configure(doc);
		Solver solver =configurer.buildSolver();
		solver.setStartingSolution(startingSolution);
		ManantialesSolution solution = (ManantialesSolution) startingSolution;
		logger.info ("Invoking solver, this should take a few minutes...");
		logger.info ("Starting distribution:");
		logger.info (getDistributions(solution));
		logger.info (solution.toString());
		solver.solve();		
		solution = (ManantialesSolution) solver.getBestSolution();
		logger.info ("Best score = " + solver.getBestScore() + " in " + 
				solver.getTimeMillisSpend() + " ms");
		logger.info (solution.toString());
		logger.info ("Final distribution:");
		logger.info (getDistributions(solution));
		assertEquals (0.0, solver.getBestScore());
	}
	
	public void testMatrixValidator () {
		MatrixGenerator generator = new MatrixGenerator ();
		Matrix matrix = new Matrix (new Distribution (Color.BLUE, 6,0,6),
				new Distribution(Color.GREEN,4,4,4), 
				new Distribution (Color.RED,6,0,6),
				new Distribution (Color.YELLOW,0,12,0));
		assertTrue ("MatrixGenerator.isViable() broken!", 
				generator.isViable(matrix));
	}
	
	/**
	 * 	Tests all possible distributions with at least 12 tokens and a score
	 *  greater than 24.
	 */
	
	public void testMatrices () {
		Solver solver =configurer.buildSolver();
		SolutionConfigurer solcon = new SolutionConfigurer(
				(ManantialesSolution) startingSolution);
		MatrixGenerator generator = new MatrixGenerator();
		/* Strange behavior from generator */
		/* Hack to test "valid" matrices" */
		Set<Matrix> matrices = generator.find();
		while (matrices.size() == 0)
			matrices = generator.find();
		assertTrue ("No matrices generated!", matrices.size() > 0);
		logger.info("Testing " + matrices.size() + " distributions for validity.\n");
		Map <Matrix, Double> goodMatrices = new HashMap<Matrix, Double>();
		Map <Matrix, Double> badMatrices = new HashMap<Matrix, Double>();
		for (Matrix matrix : matrices) {
			startingSolution = solcon.configure(matrix);
			solver.setStartingSolution(startingSolution);
			ManantialesSolution solution = (ManantialesSolution) startingSolution;
			logger.info ("Invoking solver, this should take a few minutes...");
			logger.info ("Starting distribution:");
			logger.info (getDistributions(solution));
			logger.info (solution.toString());
			solver.solve();		
			solution = (ManantialesSolution) solver.getBestSolution();
			logger.info ("Best score = " + solver.getBestScore() + " in " + 
					solver.getTimeMillisSpend() + " ms");
			logger.info (solution.toString());
			logger.info ("Final distribution:");
			logger.info (getDistributions(solution));
			if (solver.getBestScore() != 0.0) {
				badMatrices.put(matrix, new Double (solver.getBestScore()));
			} else 
				goodMatrices.put(matrix, new Double (solver.getBestScore()));
		}
		
		logger.info ("Unsolvable matrices .....\n");
		for (Matrix matrix : badMatrices.keySet()) {
			logger.info (matrix + "\nFinal Score: " + badMatrices.get(matrix));
		}
		
		logger.info ("Solve matrices:\n");
		for (Matrix matrix : goodMatrices.keySet()) {
			logger.info (matrix.toString());
		}
	}
	
	private String getDistributions(ManantialesSolution solution) {
		String ret = "";
		for (Color color : Color.values()) {
			if (color.equals(Color.UNKNOWN))
				continue;
			Distribution dist = solution.getDistribution(color);
			ret += "[" + color + "]" + " F: " + dist.getForest() + ", M: " + 
				dist.getModerate() + ", I: " + dist.getIntensive() + ", S: " + 
				dist.getSilvopastoral() + " == " + dist.getScore() + "\n";
		}
		
		return ret;
	}
}
