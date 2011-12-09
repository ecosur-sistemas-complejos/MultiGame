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

import sun.net.ResourceManager;

import java.util.Locale;
import java.util.ResourceBundle;

public enum ConditionType {
	
	NORTHERN_BORDER_DEFORESTED, EASTERN_BORDER_DEFORESTED, SOUTHERN_BORDER_DEFORESTED,
	WESTERN_BORDER_DEFORESTED, MANANTIALES_DRY, TERRITORY_DEFORESTED, INVALID_MOVE;

    public String toString(Locale loc) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", loc);
        return bundle.getString(this.name());
    }
}
