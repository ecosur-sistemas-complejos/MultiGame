/*
 * Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
 * 
 * Licensed under the Academic Free License v. 3.0. 
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * Basic ENUM for colors used in prototype games.
 */

package mx.ecosur.multigame;

public enum Color {
	YELLOW, BLUE, RED, GREEN, UNKNOWN;

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
			default:
				ret = "U";
		}
		return ret;
	}
	
	public static Color[] playable() {
		Color[] ret = { BLUE, GREEN, YELLOW, RED };
		return ret;
	}
}
