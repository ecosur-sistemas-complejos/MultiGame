/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model.interfaces;

import mx.ecosur.multigame.enums.MoveStatus;

import java.io.Serializable;

public interface Move extends Serializable {
        
    public int getId();

    public void setId(int id);

    public MoveStatus getStatus();

    public void setStatus(MoveStatus status);

    public GamePlayer getPlayerModel ();

    public void setPlayerModel (GamePlayer player);

    public Cell getCurrentCell();

    public void setCurrentCell(Cell cellImpl);

    public Cell getDestinationCell();

    public void setDestinationCell(Cell cellImpl);
    
}
