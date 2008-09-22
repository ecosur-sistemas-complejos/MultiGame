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
}
