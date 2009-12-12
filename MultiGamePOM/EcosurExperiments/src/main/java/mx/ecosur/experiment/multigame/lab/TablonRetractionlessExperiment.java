package mx.ecosur.experiment.multigame.lab;

import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStreamReader;

import mx.ecosur.multigame.exception.InvalidRegistrationException;

import mx.ecosur.experiment.multigame.solver.tablon.TablonSolution;
import mx.ecosur.experiment.multigame.solver.tablon.SolutionConfigurer;

import org.drools.solver.core.Solver;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.SimpleScore;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.config.XmlSolverConfigurer;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Dec 10, 2009
 * Time: 10:21:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class TablonRetractionlessExperiment {

private static Logger logger = Logger.getLogger(
			TablonRetractionlessExperiment.class.getCanonicalName());
	private static String configPath =
		"/mx/ecosur/experiment/multigame/solver/tablon/tablon-standard-solver.xml";

	private XmlSolverConfigurer configurer;

    protected TablonSolution solution;

    public static void main (String... args) throws InvalidRegistrationException {
        TablonRetractionlessExperiment experiment = new TablonRetractionlessExperiment();
        experiment.setUp();
        System.out.println ("Starting solver...");
        Score score = experiment.solve();
        System.out.println ("Solution found.");
        System.out.println ("Score: " + score);
        System.out.println ("Configuration:\n " + experiment.solution.getGame().toString());
    }

	public void setUp () {
		configurer = new XmlSolverConfigurer();
		configurer.configure(new InputStreamReader(this.getClass()
				.getResourceAsStream(configPath)));
        solution = new TablonSolution ();
	}

	public Score solve () throws InvalidRegistrationException {
		Solver solver = configurer.buildSolver();
        SolutionConfigurer solcon = new SolutionConfigurer((TablonSolution) solution);
        Solution startingSolution = solcon.configure();
        solver.setStartingSolution(startingSolution);
		solver.solve();
		solution = (TablonSolution) solver.getBestSolution();
        return solution.getScore();
	}
}
