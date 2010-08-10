package mx.ecosur.experiment.multigame.solver.tablon;

import mx.ecosur.multigame.impl.entity.pasale.PasaleFicha;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGrid;
import mx.ecosur.multigame.impl.enums.pasale.TokenType;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.score.Score;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.awt.*;
import java.util.Set;

import mx.ecosur.multigame.impl.entity.pasale.PasaleGame;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Nov 27, 2009
 * Time: 6:48:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class PasaleSolution implements Solution {

    private Score score;

    private Set<PasaleFicha> workingFacts;

    private PasaleGame game;

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


    public PasaleSolution() {
        super();
        workingFacts = new LinkedHashSet<PasaleFicha>();
    }

    /**
     * Constructs a new TablonSolution based on a TablonGame game.
     *
     * @param game
     */
    public PasaleSolution(PasaleGame game) {
        this();
        for (Object obj : game.getFacts()) {
            if (obj instanceof PasaleFicha) {
                workingFacts.add((PasaleFicha) obj);
            }
        }
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
     * Called by the {@link org.drools.planner.core.Solver} when the Score of this Solution has been calculated.
     *
     * @param score null if the Solution has changed and the new Score has not yet been recalculated
     */
    public void setScore(Score score) {
        this.score = score;
    }

    /**
     * Called by the {@link org.drools.planner.core.Solver} when the solution needs to be asserted into an empty WorkingMemory.
     * These facts can be used by the score rules.
     *
     * @return never null (although an empty collection is allowed), all the facts of this solution
     */
    public Collection<? extends Object> getFacts() {
        if (workingFacts.size() == 0) {
            for (Object obj : game.getFacts()) {
                if (obj instanceof PasaleFicha) {
                    workingFacts.add((PasaleFicha) obj);
                }
            }
        }

       return this.workingFacts;  
    }

    /**
     * Called by the {@link org.drools.planner.core.Solver} when the solution needs to be cloned,
     * for example to store a clone of the current solution as the best solution.
     * <p/>
     * A clone must also shallow copy the score.
     *
     * @return never null, a clone of which the properties that change during solving are deep cloned
     */
    public Solution cloneSolution() {
        PasaleSolution ret = new PasaleSolution((PasaleGame) getGame());
        ret.setScore (score);
        return ret;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append (summary(getGame()) + "\n");
        for (int y = 0; y < game.getDimensions().getWidth(); y++) {
            for (int x = 0; x < game.getDimensions().getHeight(); x++) {
                PasaleFicha ficha = findCell (x,y);
                if (ficha != null) {
                    switch (ficha.getType()) {
                        case SOIL_PARTICLE:
                            ret.append("S");
                            break;
                        case FOREST:
                            ret.append("F");
                            break;
                        case POTRERO:
                            ret.append("P");
                            break;
                        case SILVOPASTORAL:
                            ret.append("V");
                            break;
                        case WATER_PARTICLE:
                            ret.append("W");
                            break;
                        default:
                            ret.append ("U");
                            break;
                    }
                } else {
                    ret.append(" ");
                }

                /* space out the cells */
                ret.append (" ");
            }
            ret.append("\n");
        }

        return ret.toString();
    }

    public PasaleGame getGame () {
        game.setGrid(getGrid());
        return game;
    }

    public void setGame(PasaleGame game) {
        this.game = game;
        game.setGrid(getGrid());
    }
        
    private PasaleGrid getGrid() {
        PasaleGrid ret = new PasaleGrid();
        for (PasaleFicha ficha : this.workingFacts) {
            ret.updateCell(ficha);
        }
        return ret;
    }


    private PasaleFicha findCell (int y, int x) {
        PasaleFicha ret = null;
        for (PasaleFicha ficha : this.workingFacts) {
            if (ficha.getRow() == x && ficha.getColumn() == y) {
                ret = ficha;
                break;
            }
        }

        return ret;
    }

    private String summary (PasaleGame game) {
        int size = workingFacts.size();

        /* count forest */
        int forest = count (TokenType.FOREST);
        /* count potrero */
        int potrero = count (TokenType.POTRERO);
        /* count silvopastoral */
        int silvopastoral = count (TokenType.SILVOPASTORAL);

        float forestPercent = (forest/size);
        float potreroPercent = (potrero/size);
        float silvoPercent = (silvopastoral/size);

        StringBuffer buf = new StringBuffer ();
        buf.append ("Forest = " + forest + "/" + size + ", potrero = " + potrero + "/" + size + ", " +
                "silvo = " + silvopastoral + "/" + size);
        return buf.toString();
    }

    private int count (TokenType type) {
        int count = 0;
        for (PasaleFicha ficha : workingFacts) {
            if (ficha.getType().equals(type))
                count++;
        }

        return count;
    }
}