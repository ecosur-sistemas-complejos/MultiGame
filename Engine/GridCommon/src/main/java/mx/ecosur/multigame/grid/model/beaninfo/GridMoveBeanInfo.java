/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 *
 * GridMoveBeanInfo 
 *
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.grid.model.beaninfo;

import mx.ecosur.multigame.grid.model.GridCell;
import mx.ecosur.multigame.grid.model.GridMove;
import mx.ecosur.multigame.grid.model.GridPlayer;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.model.interfaces.GamePlayer;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

public class GridMoveBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor ret = new BeanDescriptor (GridMove.class);
        return ret;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor id = null, player = null, playerModel = null, current = null,
                destination = null, status = null;
        try {
            id = new PropertyDescriptor("id", Integer.class);
            player = new PropertyDescriptor ("player", GridPlayer.class);
            playerModel = new PropertyDescriptor ("playerModel", GamePlayer.class);
            current = new PropertyDescriptor ("current", GridCell.class);
            destination = new PropertyDescriptor ("destination", GridCell.class);
            status = new PropertyDescriptor ("status", MoveStatus.class);

        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return new PropertyDescriptor [] { id, player, playerModel, current, destination, status };
    }
}
