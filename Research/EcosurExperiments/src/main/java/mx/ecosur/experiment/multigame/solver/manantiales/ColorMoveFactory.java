package mx.ecosur.experiment.multigame.solver.manantiales;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.AbstractMoveFactory;
import org.drools.planner.core.solution.Solution;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 1/27/11
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorMoveFactory extends AbstractMoveFactory {

    public List<Move> createMoveList(Solution solution) {
        List<Move> ret = new ArrayList<Move>();
        Set<SolverFicha> facts = (Set<SolverFicha>) solution.getFacts();

        for (SolverFicha primary : facts) {
            if (!isBorder(primary))
                continue;
            for (SolverFicha secondary : facts) {
                if (!isBorder(secondary))
                    continue;
                ColorMove move = new ColorMove(primary, secondary.getColor());
                ret.add(move);
            }
        }

        return ret;

    }

    private boolean isBorder (SolverFicha ficha) {
        return (ficha.getColumn() == 4 || ficha.getRow() == 4);
    }
}
