package util;

import mx.ecosur.multigame.grid.entity.GridCell;

public class TestUtilities {

    private static int lastId = 0;

    public static void SetIds(GridCell... cells) {
        for (GridCell cell : cells) {
            cell.setId(++lastId);
        }
    }

}
