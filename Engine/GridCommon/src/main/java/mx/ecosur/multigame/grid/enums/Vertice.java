/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * Basic enum describing a directional vertice.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.grid.enums;

import java.util.Set;
import java.util.HashSet;

public enum Vertice {
        
    VERTICAL, HORIZONTAL, FORWARD, REVERSE, UNKNOWN;

    public Set<Direction> getDirections () {

        HashSet<Direction> ret = new HashSet<Direction>();

        switch (this) {
            case VERTICAL:
                    ret.add(Direction.NORTH);
                    ret.add(Direction.SOUTH);
                    break;
            case HORIZONTAL:
                    ret.add(Direction.EAST);
                    ret.add(Direction.WEST);
                    break;
            case FORWARD:
                    ret.add(Direction.NORTHEAST);
                    ret.add(Direction.SOUTHWEST);
                    break;
            case REVERSE:
                    ret.add(Direction.NORTHWEST);
                    ret.add(Direction.SOUTHEAST);
                    break;
            case UNKNOWN:
                    ret.add(Direction.UNKNOWN);
            default:
                    break;
        }

        return ret;
    }
}
