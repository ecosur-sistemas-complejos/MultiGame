package mx.ecosur.experiment.multigame.lab;

import java.util.logging.Logger;
import java.io.InputStreamReader;

import mx.ecosur.experiment.multigame.solver.pasale.PasaleSolution;
import mx.ecosur.multigame.exception.InvalidRegistrationException;

import mx.ecosur.experiment.multigame.solver.pasale.SolutionConfigurer;

import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.config.XmlSolverConfigurer;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Dec 10, 2009
 * Time: 10:21:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class PasaleRetractionlessExperiment {

private static Logger logger = Logger.getLogger(
			PasaleRetractionlessExperiment.class.getCanonicalName());
	private static String configPath =
		"/mx/ecosur/experiment/multigame/solver/pasale/pasale-standard-solver.xml";

	private XmlSolverConfigurer configurer;

    protected PasaleSolution solution;

    public static void main (String... args) throws InvalidRegistrationException {
        PasaleRetractionlessExperiment experiment = new PasaleRetractionlessExperiment();
        experiment.setUp();
        System.out.println ("Starting solver...");
        Score score = experiment.solve();
        System.out.println ("Solution found.");
        System.out.println ("Score: " + score);
        System.out.println ("Configuration:\n " + experiment.solution);
    }

	public void setUp () {
		configurer = new XmlSolverConfigurer();
		configurer.configure(new InputStreamReader(this.getClass()
				.getResourceAsStream(configPath)));
        solution = new PasaleSolution();
	}

	public Score solve () throws InvalidRegistrationException {
		Solver solver = configurer.buildSolver();
        SolutionConfigurer solcon = new SolutionConfigurer((PasaleSolution) solution);
        Solution startingSolution = solcon.configure();
        solver.setStartingSolution(startingSolution);
		solver.solve();
		solution = (PasaleSolution) solver.getBestSolution();
        return solution.getScore();
	}
}
