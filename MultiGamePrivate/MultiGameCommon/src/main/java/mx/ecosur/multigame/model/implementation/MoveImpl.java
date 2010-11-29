/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model.implementation;

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.model.GamePlayer;

public interface MoveImpl extends Implementation {
        
    public int getId();

    public void setId(int id);

    public MoveStatus getStatus();

    public void setStatus(MoveStatus status);

    public GamePlayer getPlayerModel ();

    public void setPlayerModel(GamePlayer player);

    public CellImpl getCurrentCell();

    public void setCurrentCell(CellImpl cellImpl);

    public CellImpl getDestinationCell();

    public void setDestinationCell(CellImpl cellImpl);
    
}
