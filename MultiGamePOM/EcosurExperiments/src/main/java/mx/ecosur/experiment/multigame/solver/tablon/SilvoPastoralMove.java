package mx.ecosur.experiment.multigame.solver.tablon;

import org.drools.solver.core.move.Move;
import org.drools.WorkingMemory;
import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.entity.tablon.TablonGrid;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;
import mx.ecosur.multigame.impl.Color;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Dec 9, 2009
 * Time: 9:19:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SilvoPastoralMove implements Move {

    TablonFicha ficha;

    TablonGrid grid;

    public SilvoPastoralMove (TablonFicha ficha, TablonGrid grid) {
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
        TablonFicha location = (TablonFicha) grid.getLocation(ficha);
        return (location.getType().equals(TokenType.POTRERO) && (location.getColor()).equals(Color.UNKNOWN) ||
                location.getColor().equals(ficha.getColor()));
    }

    /**
     * Called before the move is done, so the move can be evaluated and then be undone
     * without resulting into a permanent change in the solution.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} not yet modified by the move.
     * @return an undoMove which does the exact opposite of this move.
     */
    public Move createUndoMove(WorkingMemory workingMemory) {
        TablonFicha location = (TablonFicha) grid.getLocation(ficha);
        return new UndoMove (location, ficha, grid);
    }

    /**
     * Does the Move and updates the {@link org.drools.solver.core.solution.Solution} and its {@link org.drools.WorkingMemory} accordingly.
     * When the solution is modified, the {@link org.drools.WorkingMemory}'s {@link org.drools.FactHandle}s should be correctly notified,
     * otherwise the score(s) calculated will be corrupted.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} that needs to get notified of the changes.
     */
    public void doMove(WorkingMemory workingMemory) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
