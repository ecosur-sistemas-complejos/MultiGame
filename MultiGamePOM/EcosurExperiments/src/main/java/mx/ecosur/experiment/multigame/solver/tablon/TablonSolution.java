package mx.ecosur.experiment.multigame.solver.tablon;

import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.score.Score;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.io.ResourceFactory;
import org.drools.builder.*;

import java.util.Collection;
import java.util.LinkedList;
import java.awt.*;

import mx.ecosur.multigame.impl.entity.tablon.TablonPlayer;
import mx.ecosur.multigame.impl.entity.tablon.TablonGame;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.exception.InvalidRegistrationException;

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

    public enum Quadrant {
        TOPLEFT, TOPRIGHT, BOTTOMLEFT, BOTTOMRIGHT;


        public boolean contains (Dimension dimension, Point point) {
            boolean ret = false;

            int row = dimension.height / 2;
            int col = dimension.width / 2;

            switch (this) {
                case TOPLEFT:
                    ret = (point.getX() < row && point.getY() < col);
                    break;
                case TOPRIGHT:
                    ret = (point.getX() < row && point.getY() > col);
                    break;
                case BOTTOMLEFT:
                    ret = (point.getX() > row && point.getY() < col);
                    break;
                case BOTTOMRIGHT:
                    ret = (point.getX() > row && point.getY() > col);
                    break;
                default:
                    assert (false);
            }

            return ret;
        }
    }

    public TablonSolution () {
        super();
    }


    /**
     * Constructs a new TablonSolution based on a TablonGame game.
     *
     * @param game
     */
    public TablonSolution (TablonGame game) {
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
        LinkedList facts = new LinkedList();
        facts.add(game);
        return facts;        
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

    public TablonGame getGame() {
        return game;
    }

    public void setGame (TablonGame game) {
        this.game = game;
    }    
}