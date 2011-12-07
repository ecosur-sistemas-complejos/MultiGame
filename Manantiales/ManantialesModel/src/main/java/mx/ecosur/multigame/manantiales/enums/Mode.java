/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

package mx.ecosur.multigame.manantiales.enums;

/**
 * @author awaterma@ecosur.mx
 */
public enum Mode {

    COMPETITIVE, BASIC_PUZZLE, SILVOPASTORAL, SILVO_PUZZLE, RELOADED;
    
    public int getWinningScore() {
        int ret = 0;

        switch (this) {
            case COMPETITIVE:
                ret = 24;
                break;
            case BASIC_PUZZLE:
                ret = 24;
                break;
            case SILVOPASTORAL:
                ret = 32;
                break;
            case SILVO_PUZZLE:
                ret = 32;
                break;
        }

        return ret;
    }

}
