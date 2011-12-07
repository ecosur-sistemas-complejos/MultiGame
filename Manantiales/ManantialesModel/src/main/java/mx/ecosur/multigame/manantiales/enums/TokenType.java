/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.manantiales.enums;

public enum TokenType {
	
	UNKNOWN, UNDEVELOPED, MANAGED_FOREST, MODERATE_PASTURE, INTENSIVE_PASTURE, VIVERO, SILVOPASTORAL;
	
	public int value () {
		int ret = 0;
		switch (this) {
		    case UNDEVELOPED:
		    	ret = 0;
		    	break;
			case MANAGED_FOREST:
				ret = 1;
				break;
			case MODERATE_PASTURE:
				ret = 2;
				break;
			case INTENSIVE_PASTURE:
				ret = 3;
				break;
			case VIVERO:
				ret = 0;
				break;
			case SILVOPASTORAL:
				ret = 4;
				break;
			default:
				throw new RuntimeException ("Unknown Ficha! No value assigned!");
		}
		return ret;
	}
}
