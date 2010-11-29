package mx.ecosur.multigame.impl;

import mx.ecosur.multigame.model.implementation.MoveImpl;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author awaterma@ecosur.mx
 */
public class MoveComparator implements Comparator<MoveImpl>, Serializable {


    public int compare(MoveImpl o1, MoveImpl o2) {
        int ret = 0;        
        if (o1.getId() > o2.getId())
            ret = 1;
        else if (o1.getId() < o2.getId())
            ret = -1;
        return ret;
    }
}
