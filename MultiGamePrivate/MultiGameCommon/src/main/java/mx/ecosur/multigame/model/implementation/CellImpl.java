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

public interface CellImpl extends Implementation {

    public int getId();

    public void setId(int id);

    public int getRow();

    public void setRow (int row);

    public int getColumn();    

    public void setColumn (int column);

}
