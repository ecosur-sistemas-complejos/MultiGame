package mx.ecosur.experiment.multigame.solver.manantiales;

import org.drools.planner.core.Solver;
import org.drools.planner.core.solution.Solution;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 1/26/11
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class MatrixSolverThread extends Thread {

    private Matrix matrix;

    private Solver solver;

    public MatrixSolverThread (Matrix matrix, Solver solver, Solution solution) {
        super();
        this.matrix = matrix;
        this.solver = solver;
        this.solver.setStartingSolution(solution);
    }

    @Override
    public void run() {
        solver.solve();
    }

    public Solution getSolution() {
        return solver.getBestSolution();
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public long getTimeMillisSpend() {
        return solver.getTimeMillisSpend();
    }
}
