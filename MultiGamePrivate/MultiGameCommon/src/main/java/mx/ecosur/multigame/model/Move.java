/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model;

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.model.implementation.Implementation;
import mx.ecosur.multigame.model.implementation.MoveImpl;

public class Move implements Model {
        
    private static final long serialVersionUID = 8462209469604832617L;

    private MoveImpl moveImpl;

    public Move () {
        super();
    }

    public Move (MoveImpl moveImpl) {
        this.moveImpl = moveImpl;
    }

    public int getId() {
        return moveImpl.getId();
    }

    public MoveStatus getStatus() {
        return moveImpl.getStatus();
    }

    public void setStatus(MoveStatus status) {
        moveImpl.setStatus(status);
    }

    public GamePlayer getPlayer () {
        return moveImpl.getPlayerModel();
    }

    public void setPlayer (GamePlayer player) {
        moveImpl.setPlayerModel (player);
    }

    public Cell getCurrent () {
        return new Cell(moveImpl.getCurrentCell());
    }

    public void setCurrent (Cell current) {
        moveImpl.setCurrentCell(current.getImplementation());
    }

    public Cell getDestination() {
        return new Cell (moveImpl.getDestinationCell());
    }

    public void setDestination (Cell destination) {
        moveImpl.setDestinationCell (destination.getImplementation());
    }

    public MoveImpl getImplementation() {
        return moveImpl;
    }

    public void setImplementation(Implementation impl) {
        this.moveImpl = (MoveImpl) impl;
    }

    public String toString () {
        String ret = null;
        if (moveImpl != null)
            ret = moveImpl.toString();
        else
            ret = super.toString();
        return ret;
    }
}
