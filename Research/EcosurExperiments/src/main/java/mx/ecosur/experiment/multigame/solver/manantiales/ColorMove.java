package mx.ecosur.experiment.multigame.solver.manantiales;

import mx.ecosur.multigame.grid.Color;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.planner.core.move.Move;

/**
 * Changes a tokens color (the territory owner)
 * from one to another player.  Only valid if
 * both tokens share the same border.
 */
public class ColorMove implements Move {

    private SolverFicha target;

    private Color color;

    public ColorMove (SolverFicha target, Color color) {
        this.target =target;
        this.color = color;
    }

    @Override
    public boolean isMoveDoable(WorkingMemory wm) {
        boolean ret = false;
        switch (color) {
            case BLUE:
                if (target.getColumn() < 4)
                    ret = target.getColor().equals(Color.RED);
                else
                    ret = (target.getColor().equals(Color.GREEN));
                break;
            case YELLOW:
                if (target.getColumn() < 4)
                    ret = target.getColor().equals(Color.GREEN);
                else
                    ret = target.getColor().equals(Color.RED);
                break;
            case GREEN:
                if (target.getColumn() < 4)
                    ret = target.getColor().equals(Color.BLUE);
                else
                    ret = target.getColor().equals(Color.YELLOW);
                break;
            case RED:
                if (target.getColumn() < 4)
                    ret = target.getColor().equals(Color.BLUE);
                else
                    ret = target.getColor().equals(Color.YELLOW);
                break;
            default:
                break;
        }

        ret = ret && (wm.getFactHandle(target) != null);

        /* Border Check */
        return ret && (target.getColumn() == 4 || target.getRow() == 4);

    }

    @Override
    public Move createUndoMove(WorkingMemory workingMemory) {
        return new ColorMove(target, target.getColor());
    }

    @Override
    public void doMove(WorkingMemory wm) {
        FactHandle fh = wm.getFactHandle (target);
            try {
                SolverFicha clone = (SolverFicha) target.clone();
                clone.setColor(color);
                wm.update(fh,clone);
            } catch (CloneNotSupportedException e) { throw new RuntimeException(e);}
    }

    @Override
    public String toString() {
	    return "change color [ " + target.getColor() + " => " + color + "] @ " + target;
    }
}
