package mx.ecosur.experiment.multigame.solver.tablon;

import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.entity.tablon.TablonGrid;
import mx.ecosur.multigame.impl.entity.tablon.TablonGame;
import mx.ecosur.multigame.impl.entity.tablon.TablonMove;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;

import org.drools.solver.core.move.Move;
import org.drools.WorkingMemory;
import org.drools.runtime.rule.FactHandle;

/**
 * 
 *  
 */
public class PotreroMove implements Move {

    private TablonGame game;

    private TablonFicha ficha;

    public PotreroMove(TablonGame game, TablonFicha ficha) {
        this.game = game;
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
        boolean ret = false;
        try {
            GridPlayer current = null;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    current = player;
                    break;
                }
            }

            /* must be a player with a turn */
            assert (current != null);
            int starting = game.getGrid().getCells().size();
            TablonMove move = new TablonMove (current, ficha);
            move = (TablonMove) game.move(move);
            int ending = game.getGrid().getCells().size();

            /* To be valid, the move must have been evaluated and no retractions occurred */
            ret = (move.getStatus().equals(MoveStatus.EVALUATED) && starting == ending);
        } catch (InvalidMoveException e) {
            //
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
        TablonFicha location = (TablonFicha) game.getGrid().getLocation(ficha);
        UndoMove ret = null;
        try {
            ret = new UndoMove ((TablonGame) game.clone(), location, ficha);
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
        grid.updateCell(ficha);
        workingMemory.retract(handle);
        workingMemory.insert(game);
    }
}
