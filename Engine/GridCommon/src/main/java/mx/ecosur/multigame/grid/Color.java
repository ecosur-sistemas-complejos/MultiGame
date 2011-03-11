/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 * 
 * Licensed under the Academic Free License v. 3.0. 
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * Basic ENUM for colors used in prototype games.
 *
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.grid;

public enum Color {
    YELLOW, BLUE, PURPLE, RED, GREEN, BLACK, UNKNOWN;

    public Color getCompliment() {
        Color ret;

        switch (this) {
        case RED:
            ret = YELLOW;
            break;
        case GREEN:
            ret = BLUE;
            break;
        case BLUE:
            ret = GREEN;
            break;
        case YELLOW:
            ret = RED;
            break;
        case PURPLE:
            ret = YELLOW;
            break;
        case BLACK:
            ret = RED;
            break;
        default:
            ret = UNKNOWN;
        }

        return ret;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        String ret = "";
        switch (this) {
            case RED:
                ret = "R";
                break;
            case GREEN:
                ret = "G";
                break;
            case BLUE:
                ret = "B";
                break;
            case YELLOW:
                ret = "Y";
                break;
           case PURPLE:
                ret = "P";
                break;
           case BLACK:
                ret = "B";
                break;
            default:
                ret = "U";
        }
        return ret;
    }

    public static Color[] playable() {
            Color[] ret = { BLUE, GREEN, YELLOW, RED, PURPLE, BLACK};
            return ret;
    }
}
