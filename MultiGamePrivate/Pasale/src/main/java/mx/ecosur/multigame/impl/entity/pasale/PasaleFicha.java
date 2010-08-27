/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
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
import javax.persistence.Id;

import org.apache.commons.lang.builder.HashCodeBuilder;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.enums.pasale.*;

@Entity ()
public class PasaleFicha extends GridCell {

    private static final long serialVersionUID = -8048552960014554186L;
    private TokenType type;
    private Color[] passThroughColors;

    public PasaleFicha() {
        super();
    }

    public PasaleFicha(int column, int row, Color color, TokenType type) {
        super(column, row, color);
        this.type = type;
        passThroughColors = Color.values();
    }    

    @Enumerated (EnumType.STRING)
    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof PasaleFicha) {
            PasaleFicha comp = (PasaleFicha) obj;
            ret = (comp.type.equals(this.type) &&
                    comp.getRow() == getRow() &&
                    comp.getColumn() == getColumn() &&
                    comp.getColor() == getColor());
        }

        return ret;
    }

    @Override
    public PasaleFicha clone() throws CloneNotSupportedException {
        GridCell clone = super.clone();
        PasaleFicha ret = new PasaleFicha();
        ret.setRow(clone.getRow());
        ret.setColumn(clone.getColumn());
        ret.setColor(clone.getColor());
        ret.setType(this.type);
        return ret;

    }/* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(getColumn())
        .append(getRow())
        .append(getType())
        .append(getColor())
        .toHashCode();
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.model.Cell#toString()
     */
    @Override
    public String toString() {
        return "[" + this.getClass().getName() + "], type = " + type + ", " + super.toString();
    }
    }
