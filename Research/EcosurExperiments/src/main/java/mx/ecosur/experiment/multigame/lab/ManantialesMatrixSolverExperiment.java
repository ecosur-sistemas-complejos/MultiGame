/*
* Copyright (C) 2008-2011 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.experiment.multigame.lab;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.experiment.multigame.solver.manantiales.*;
import mx.ecosur.multigame.grid.util.CSV;

import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.SimpleScore;
import org.drools.planner.core.solution.Solution;
import org.jdom.Document;
import org.jdom.JDOMException;

/**
 * This test ensures that the solver is configured and working correctly.
 * The intention is to use 
 */


public class ManantialesMatrixSolverExperiment {

    private static Logger logger = Logger.getLogger(
            ManantialesMatrixSolverExperiment.class.getCanonicalName());
    private static String configPath =
        "/mx/ecosur/experiment/multigame/solver/manantiales/manantiales-standard-solver.xml";
    private static String testModelPath =
        "/mx/ecosur/experiment/multigame/solver/manantiales/data/testSolution.xml";
    private static String asymmetricPath =
        "/mx/ecosur/experiment/multigame/solver/manantiales/data/distribution.xml";
    private static String csvPath =
        "/mx/ecosur/experiment/multigame/solver/manantiales/data/matrices.csv";

    private XmlSolverConfigurer configurer;
    private Solution startingSolution;
    private ManantialesSolution solution;

    public static void main (String... args) throws UnconfigurableException, JDOMException, IOException {
        ManantialesMatrixSolverExperiment experiment = new ManantialesMatrixSolverExperiment();
        experiment.setUp();
        System.out.println ("Starting solver...");
        experiment.testCSVMatrices();
        System.out.println ("Solution complete.");
    }

    public void setUp () {
        solution = null;
        configurer = new XmlSolverConfigurer();
        configurer.configure(new InputStreamReader(this.getClass()
                .getResourceAsStream(configPath)));
        startingSolution = new ManantialesSolution(
                ManantialesSolution.Threshold.SIMPLE);
    }

    public void testSolver () throws JDOMException, IOException,
        UnconfigurableException
    {
        SolutionConfigurer solcon = new SolutionConfigurer(
                (ManantialesSolution) startingSolution);
        Document doc = solcon.load(testModelPath);
        startingSolution = solcon.configure(doc);
        Solver solver = configurer.buildSolver();
        solver.setStartingSolution(startingSolution);
        solver.solve();
        solution = (ManantialesSolution) solver.getBestSolution();
        SimpleScore score = (SimpleScore) solution.getScore();
    }

    public void testDistribution () throws JDOMException, IOException,
        UnconfigurableException
    {
        SolutionConfigurer solcon = new SolutionConfigurer(
                (ManantialesSolution) startingSolution);
        Document doc = solcon.load(asymmetricPath);
        startingSolution = solcon.configure(doc);
        Solver solver = configurer.buildSolver();
        solver.setStartingSolution(startingSolution);
        ManantialesSolution solution = (ManantialesSolution) startingSolution;
        logger.info ("Invoking solver, this may take a few moments...");
        logger.info (getDistributions(solution));
        logger.info (solution.toString());
        solver.solve();
        solution = (ManantialesSolution) solver.getBestSolution();
        logger.info (getDistributions(solution));
        logger.info ("Score: " + solution.getScore().toString());
        logger.info (solution.toString());
        logger.info ("Solved in: " + solver.getTimeMillisSpend() + " ms.");
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
     * @throws UnconfigurableException
     */

    public void testGeneratedMatrices () throws UnconfigurableException {
        SolutionConfigurer solcon = new SolutionConfigurer(
                (ManantialesSolution) startingSolution);
        MatrixGenerator generator = new MatrixGenerator();
        Set<Matrix> matrices = generator.find();
        while (matrices.size() == 0)
            matrices = generator.find();
        assertTrue ("No matrices generated!", matrices.size() > 0);
        logger.info("Testing " + matrices.size() + " distributions for validity.\n");
        Map <Matrix, Score> goodMatrices = new HashMap<Matrix, Score>();
        Map <Matrix, Score> badMatrices = new HashMap<Matrix, Score>();
        StringBuffer log = new StringBuffer();
        for (Matrix matrix : matrices) {
            log.delete(0, log.length());
            Solver solver =configurer.buildSolver();
            startingSolution = solcon.configure(matrix);
            try {
                solver.setStartingSolution(startingSolution);
                ManantialesSolution solution = (ManantialesSolution) startingSolution;
                log.append("Starting matrix:\n" + matrix.toString());
                log.append (matrix.toString());
                logger.info (log.toString());
                solver.solve();
                solution = (ManantialesSolution) solver.getBestSolution();
                log = log.delete(0, log.length());
                log.append("Final distribution:\n" + getDistributions(solution));
                log.append(solution.toString());
                log.append("Best score = " + solution.getScore() + " in " +
                        solver.getTimeMillisSpend() + " ms");
                logger.info (log.toString());
                if (!solution.getScore().equals(0.0)) {
                    badMatrices.put(matrix, solution.getScore());
                } else
                    goodMatrices.put(matrix, solution.getScore());
            } catch (RuntimeException e) {
                badMatrices.put(matrix, null);
            }
        }

        logger.info ("Unsolvable matrices .....\n");
        for (Matrix matrix : badMatrices.keySet()) {
            logger.info (matrix + "\nFinal Score: " + badMatrices.get(matrix));
        }

        logger.info ("Solvable matrices:\n");
        for (Matrix matrix : goodMatrices.keySet()) {
            logger.info (matrix.toString());
        }
    }

    public void testCSVMatrices () throws IOException {
        BufferedReader reader = new BufferedReader (new InputStreamReader(this.getClass()
                .getResourceAsStream(csvPath)));
        SolutionConfigurer solcon = new SolutionConfigurer(
                (ManantialesSolution) startingSolution);
        Map <Matrix, Score> goodMatrices = new HashMap<Matrix, Score>();
        Map <Matrix, Score> badMatrices = new HashMap<Matrix, Score>();
        List<Matrix> unconfigurable = new ArrayList<Matrix>();
        logger.info ("Reading in CSV matrices from file and solving ...");
        int counter = 0;
        List<MatrixSolverThread> threads = new ArrayList<MatrixSolverThread>();
        while (reader.ready()) {
            Matrix matrix = readMatrix (reader);
            try {
                logger.info ("Finding solution for Matrix #" + counter  + " @ [" +
                        new Date(System.currentTimeMillis()) + "]");
                startingSolution = solcon.configure(matrix);
                logger.info ("Starting Solution: " + startingSolution);
                System.out.println ("Starting Distribution: " + getDistributions((ManantialesSolution) startingSolution));
                MatrixSolverThread thread = new MatrixSolverThread (matrix, configurer.buildSolver(), startingSolution);
                thread.start();
                thread.join();
                threads.add(thread);
                counter++;

            } catch (UnconfigurableException e) {
                unconfigurable.add(matrix);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (MatrixSolverThread t : threads) {
            ManantialesSolution solution = (ManantialesSolution) t.getSolution();
            logger.info ("Distributions: "+ getDistributions ((ManantialesSolution) t.getSolution()));
            logger.info ("Matrix: " + t.getMatrix().toString());
            logger.info ("Best score = " + solution.getScore() + " in " +
                    t.getTimeMillisSpend() + " ms");
            logger.info ("-------------\n" + solution.toString() +
                    "\n-------------\n");
            if (!solution.getScore().equals(0.0)) {
                badMatrices.put(t.getMatrix(), solution.getScore());
            } else
                goodMatrices.put(t.getMatrix(), solution.getScore());
        }

        logger.info ("---------------------------------------------");
        logger.info ("Unconfigurable matrices .....\n");
        for (Matrix matrix : unconfigurable) {
            logger.info (matrix.toString());
        }

        logger.info ("---------------------------------------------");
        logger.info ("Unsolvable matrices .....\n");
        for (Matrix matrix : badMatrices.keySet()) {
            logger.info (matrix + "\nFinal Score: " + badMatrices.get(matrix));
        }

        logger.info ("---------------------------------------------");
        logger.info ("Solvable matrices:\n");
        for (Matrix matrix : goodMatrices.keySet()) {
            logger.info (matrix.toString());
        }

        int unconfigs = unconfigurable.size();
        int bad = badMatrices.size();
        int good = goodMatrices.size();
        int total = unconfigs + bad + good;

        logger.info ("Summary:");
        logger.info ("Total Matrices tested: " + total);
        logger.info ("Unconfigurable matrices: " + unconfigs);
        logger.info ("Unsolvable matrices: " + bad);
        logger.info ("Solvable matrices: " + good);
    }
    /**
     * @param reader
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private Matrix readMatrix(BufferedReader reader) throws IOException {
        CSV csv = new CSV();
        List<Distribution> distributions = new ArrayList<Distribution>();
        Color[] colors = Color.playable();
        int color = 0;
        while (distributions.size() < 4) {
            String line = reader.readLine();
            if (line == null || line.equals("\n") ||
                    line.indexOf("TOKENS") > 0 || line.equals(",,"))
                continue;
            List<String> distLine = csv.parse(line);

            int forest = 0, managed = 0, intensive = 0, silvo = 0, counter = 0;
            for (String str : distLine) {
                if (counter == 0)
                    forest = Integer.parseInt(str);
                else if (counter == 1)
                    managed = Integer.parseInt(str);
                else if (counter == 2)
                    intensive = Integer.parseInt(str);
                else if (counter == 3)
                    silvo = Integer.parseInt(str);
                counter++;
            }

            distributions.add(new Distribution(colors [ color++], forest, managed,
                    intensive, silvo));
        }

        return new Matrix(distributions);
    }

    private String getDistributions(ManantialesSolution solution) {
        String ret = "";
        for (Color color : Color.values()) {
            if (color.equals(Color.UNKNOWN) || color.equals(Color.PURPLE) || color.equals(Color.BLACK))
                continue;
            Distribution dist = solution.getDistribution(color);
            ret += "[" + color + "," + dist.getCount() + "]";
            ret += "F: " + dist.getForest() + ", M: " +
                dist.getModerate() + ", I: " + dist.getIntensive() + ", S: " +
                dist.getSilvopastoral() + " == " + dist.getScore() + "\n";
        }

        return ret;
    }
}
