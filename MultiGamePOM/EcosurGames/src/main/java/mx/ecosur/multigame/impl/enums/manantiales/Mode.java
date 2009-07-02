/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.enums.manantiales;

public enum Mode {
	
    CLASSIC, BASIC_PUZZLE, SILVOPASTORAL, SILVO_PUZZLE, RELOADED;
    
    public int getWinningScore() {
    	int ret = 0;
    	
    	switch (this) {
        	case CLASSIC:
        		ret = 24;
        		//ret = 8;
        		break;
        	case BASIC_PUZZLE:
        		ret = 24;
        		break;
        	case SILVOPASTORAL:
        		ret = 32;
        		//ret = 8;
        		break;
        	case SILVO_PUZZLE:
        		ret = 32;
        		break;
        	case RELOADED:
        		ret = 32;
        		break;	        		        	
    	}
    	
    	return ret;
    }
}
