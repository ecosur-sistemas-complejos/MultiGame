package mx.ecosur.experiment.multigame.solver.tablon;

import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.score.Score;

import java.util.Collection;

import mx.ecosur.multigame.impl.entity.tablon.TablonGrid;
import mx.ecosur.multigame.impl.entity.tablon.TablonPlayer;
import mx.ecosur.multigame.impl.entity.tablon.TablonMove;
import mx.ecosur.multigame.impl.entity.tablon.TablonGame;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Nov 27, 2009
 * Time: 6:48:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class TablonSolution implements Solution {

    private Score score;

    private TablonGame game;


    /**
     * Constructs a new TablonSolution based on a TablonGame game.
     *
     * @param game
     */
    public TablonSolution (TablonGame game) {
        this.score = null;
        this.game = game;
    }

    /**
     * Returns the Score of this Solution.
     *
     * @return null if the Solution is unitialized
     *         or the last calculated Score is dirty the new Score has not yet been recalculated
     */
    public Score getScore() {
        return score;
    }

    /**
     * Called by the {@link org.drools.solver.core.Solver} when the Score of this Solution has been calculated.
     *
     * @param score null if the Solution has changed and the new Score has not yet been recalculated
     */
    public void setScore(Score score) {
        this.score = score;
    }

    /**
     * Called by the {@link org.drools.solver.core.Solver} when the solution needs to be asserted into an empty WorkingMemory.
     * These facts can be used by the score rules.
     *
     * @return never null (although an empty collection is allowed), all the facts of this solution
     */
    public Collection<? extends Object> getFacts() {
        return game.getFacts();
    }

    /**
     * Called by the {@link org.drools.solver.core.Solver} when the solution needs to be cloned,
     * for example to store a clone of the current solution as the best solution.
     * <p/>
     * A clone must also shallow copy the score.
     *
     * @return never null, a clone of which the properties that change during solving are deep cloned
     */
    public Solution cloneSolution() {
        TablonSolution ret = null;
        try {
            ret = new TablonSolution((TablonGame) game.clone());
            ret.setScore (score);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return ret;
    }

    public TablonGrid getGrid () {
        TablonGrid grid = null;
        try {
            grid = (TablonGrid) game.getGrid().clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return grid;       
    }

    public TablonGame getGame() {
        TablonGame ret = null;
        try {
            ret = (TablonGame) game.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ret;
    }
}