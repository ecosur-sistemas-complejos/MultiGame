package mx.ecosur.multigame.grid;

import mx.ecosur.multigame.model.interfaces.Move;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author awaterma@ecosur.mx
 */
public class MoveComparator implements Comparator<Move>, Serializable {


    public int compare(Move o1, Move o2) {
        int ret = 0;        
        if (o1.getId() > o2.getId())
            ret = 1;
        else if (o1.getId() < o2.getId())
            ret = -1;
        return ret;
    }
}
