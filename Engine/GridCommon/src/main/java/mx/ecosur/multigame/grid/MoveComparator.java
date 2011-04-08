package mx.ecosur.multigame.grid;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.model.GridCell;
import mx.ecosur.multigame.model.interfaces.Cell;
import mx.ecosur.multigame.model.interfaces.Move;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author awaterma@ecosur.mx
 */
public class MoveComparator implements Comparator<Move>, Serializable {


    public int compare(Move o1, Move o2) {
        int ret = 0;
        if (o1.getId() > 0 && o2.getId() > 0) {
            if (o1.getId() > o2.getId())
                ret = 1;
            else if (o1.getId() < o2.getId())
                ret = -1;
        } else {
            CellComparator c = new CellComparator();
            ret = c.compare((GridCell) o1.getDestinationCell(), (GridCell) o2.getDestinationCell());
        }

        return ret;

    }
}
