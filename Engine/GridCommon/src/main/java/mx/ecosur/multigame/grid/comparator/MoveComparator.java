package mx.ecosur.multigame.grid.comparator;

import mx.ecosur.multigame.grid.model.GridMove;
import mx.ecosur.multigame.model.interfaces.Move;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/15/11
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveComparator implements Comparator<GridMove>, Serializable {

    @Override
    public int compare(GridMove move, GridMove move1) {
        int ret = 0;

        if (move.getId() != move1.getId()) {
            if (move.getId() < move1.getId())
                ret = -1;
            else if (move.getId() > move1.getId())
                ret = 1;
        }

        return ret;
    }
}
