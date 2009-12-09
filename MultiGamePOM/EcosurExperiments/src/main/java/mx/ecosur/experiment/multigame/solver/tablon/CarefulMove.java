package mx.ecosur.experiment.multigame.solver.tablon;

import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.entity.tablon.TablonMove;

import java.util.Stack;

import org.drools.solver.core.move.Move;
import org.drools.WorkingMemory;
import org.drools.FactHandle;

/**
 * A CarefulMove is a move that shouldn't elicit retractions from the game.
 *  
 */
public class CarefulMove implements Move {

    private TablonFicha ficha;

    public CarefulMove(TablonFicha ficha) {
        this.ficha = ficha;
    }

    /**
     * Called before a move is evaluated to decide wheter the move can be done and evaluated.
     * A Move isn't doable if:
     * <ul>
     * <li>Either doing it would change nothing in the solution.</li>
     * <li>Either it's simply not possible to do.</li>
     * </ul>
     * Although you could filter out non-doable moves in for example the {@link org.drools.solver.core.move.factory.MoveFactory},
     * this is not needed as the {@link org.drools.solver.core.Solver} will do it for you.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} not yet modified by the move.
     * @return true if the move achieves a change in the solution and the move is possible to do on the solution.
     */
    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return false;
    }

    /**
     * Called before the move is done, so the move can be evaluated and then be undone
     * without resulting into a permanent change in the solution.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} not yet modified by the move.
     * @return an undoMove which does the exact opposite of this move.
     */
    public Move createUndoMove(WorkingMemory workingMemory) {
        CarefulMove ret = null;

/*        if (move != null) {
            moveHandle = workingMemory.insert(move);
            workingMemory.retract(moveHandle);
            moveHandle = null;
            ret = new CarefulMove(move);*/
        

        return ret;
    }

    /**
     * Does the Move and updates the {@link org.drools.solver.core.solution.Solution} and its {@link org.drools.WorkingMemory} accordingly.
     * When the solution is modified, the {@link org.drools.WorkingMemory}'s {@link org.drools.FactHandle}s should be correctly notified,
     * otherwise the score(s) calculated will be corrupted.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} that needs to get notified of the changes.
     */
    public void doMove(WorkingMemory workingMemory) {
/*        if (moveHandle != null) {
            workingMemory.retract(moveHandle);
        } else {
            moveHandle = workingMemory.insert(move);*/

    }
}
