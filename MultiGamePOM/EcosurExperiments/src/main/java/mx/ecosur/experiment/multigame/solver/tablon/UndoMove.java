package mx.ecosur.experiment.multigame.solver.tablon;

import org.drools.solver.core.move.Move;
import org.drools.WorkingMemory;
import org.drools.runtime.rule.FactHandle;
import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.entity.tablon.TablonGrid;
import mx.ecosur.multigame.impl.entity.tablon.TablonGame;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Dec 9, 2009
 * Time: 9:52:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class UndoMove implements Move {

    /* The move to do */
    private TablonFicha previous;

    /* The move to be undone */
    private TablonFicha current;

    private TablonGame game;

    public UndoMove (TablonGame game, TablonFicha previousFicha, TablonFicha currentFicha) {
        this.game = game;
        this.previous = previousFicha;
        this.current = currentFicha;
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
        boolean ret = false;

        if (current.getType().equals(TokenType.POTRERO)) {
            ret = previous.getType().equals(TokenType.FOREST);
        } else if (current.getType().equals(TokenType.SILVOPASTORAL)) {
            ret = previous.getType().equals(TokenType.POTRERO);
        }

        return ret;
    }

    /**
     * Called before the move is done, so the move can be evaluated and then be undone
     * without resulting into a permanent change in the solution.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} not yet modified by the move.
     * @return an undoMove which does the exact opposite of this move.
     */
    public Move createUndoMove(WorkingMemory workingMemory) {
        UndoMove ret = null;
        try {
            ret = new UndoMove ((TablonGame) game.clone(), current, previous);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
        TablonGame game = null;
        FactHandle handle = null;

        for (Object obj : workingMemory.getObjects()) {
            if (obj instanceof TablonGame) {
                game = (TablonGame) obj;
                handle =workingMemory.getFactHandle(game);
                break;
            }
        }

        /* Must have a game in memory */
        assert (game != null);
        assert (handle != null);
        TablonGrid grid = (TablonGrid) game.getGrid();
        grid.removeCell(current);
        grid.updateCell(previous);
        workingMemory.retract(handle);
        workingMemory.insert(game);        
    }
}
