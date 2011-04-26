/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.pasale;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import mx.ecosur.multigame.grid.entity.GridMove;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.impl.enums.pasale.TokenType;

import java.util.*;

@Entity
public class PasaleMove extends GridMove {

    private static final long serialVersionUID = 1L;

    private TokenType type, replacementType;

    private boolean badYear, premium;

    private Stack<PasaleFicha> path;

    public PasaleMove() {
        super();
    }

    public PasaleMove(GridPlayer player, PasaleFicha destination) {
        super (player, destination);
        type = destination.getType();
    }

    public PasaleMove(GridPlayer player, PasaleFicha current, PasaleFicha destination)
    {
        super (player, current, destination);
    }

    @Enumerated(EnumType.STRING)
    public TokenType getType () {
        return type;
    }

    public void setType (TokenType type) {
        this.type = type;
    }

    @Transient
    public TokenType getReplacementType() {
        replacementType = TokenType.UNKNOWN;
        if (getCurrentCell() instanceof PasaleFicha) {
            PasaleFicha current = (PasaleFicha) getCurrentCell();
            replacementType = current.getType();
        }

        return replacementType;
    }

    public void setReplacementType(TokenType replacementType) {
        this.replacementType = replacementType;
    }

    public boolean isBadYear () {
        return badYear;
    }

    public void setBadYear (boolean year) {
        badYear = year;
    }


    @Override
    public int hashCode() {
       int curCode = 1, destCode = 1;
       if (current != null)
        curCode = curCode - current.hashCode();
       if (destination != null)
         destCode = destCode + destination.hashCode();
       return 31 * curCode + destCode;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof PasaleMove) {
            PasaleMove comparison = (PasaleMove) obj;
            if (current != null && destination !=null) {
                ret = current.equals( (comparison.getCurrentCell())) &&
                      destination.equals(comparison.getDestinationCell());
            } else if (destination != null) {
                ret = destination.equals(comparison.getDestinationCell());
              }
        }

        return ret;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PasaleMove ret = new PasaleMove();
        ret.setBadYear(this.badYear);
        ret.setPlayer(this.getPlayer());
        ret.setReplacementType(this.getReplacementType());
        ret.setCurrentCell(this.getCurrentCell());
        ret.setDestinationCell(this.getDestinationCell());
        ret.setStatus(this.getStatus());
        ret.setPlayerModel(this.getPlayer());
        return ret;
    }
}
