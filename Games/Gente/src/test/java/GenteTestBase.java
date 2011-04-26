/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

import mx.ecosur.multigame.grid.entity.GridCell;

public class GenteTestBase {
    private static int lastId;

    public static void setIds (GridCell... cells) {
        for (GridCell cell : cells) {
            cell.setId(++lastId);
        }
    }
}
