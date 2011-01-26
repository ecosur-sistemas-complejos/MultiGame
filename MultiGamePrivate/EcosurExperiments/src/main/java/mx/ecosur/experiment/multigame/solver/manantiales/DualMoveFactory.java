/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mx.ecosur.experiment.multigame.solver.manantiales;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.AbstractMoveFactory;
import org.drools.planner.core.solution.Solution;

/**
 *
 * @author awaterma
 */
public class DualMoveFactory extends AbstractMoveFactory {
    
    private RotateMoveFactory rotateFactory = new RotateMoveFactory();

    private SwapMoveFactory swapFactory = new SwapMoveFactory();

    @Override
    public List<Move> createMoveList(Solution sltn) {
        List<Move> ret = swapFactory.createMoveList(sltn);
        ret.addAll(rotateFactory.createMoveList(sltn));
        return ret;
    }
}
