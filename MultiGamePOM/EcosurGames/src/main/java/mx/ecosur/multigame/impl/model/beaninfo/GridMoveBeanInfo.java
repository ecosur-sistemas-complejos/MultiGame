/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
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
package mx.ecosur.multigame.impl.model.beaninfo;

import mx.ecosur.multigame.impl.model.GridMove;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.enums.MoveStatus;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

/**
 * Created by IntelliJ IDEA.
 * User: awater
 * Date: 22-oct-2009
 * Time: 10:13:57
 * To change this template use File | Settings | File Templates.
 */
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
        PropertyDescriptor [] ret = { id, player, playerModel, current, destination, status };
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

        return ret;
    }
}
