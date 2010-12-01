/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 *
 * GridGameBeanInfo 
 *
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl.model.beaninfo;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Date;
import java.util.List;

import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridGame;

public class GridGameBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
         BeanDescriptor ret = new BeanDescriptor (GridGame.class);
        return ret;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor id = null, rows = null, columns = null, players = null, created= null,
                state = null, grid = null, type = null;

        try {
            id = new PropertyDescriptor("id", Integer.class);
            rows = new PropertyDescriptor("rows", Integer.class);
            columns = new PropertyDescriptor("columns", Integer.class);
            players = new PropertyDescriptor("players", List.class);
            created = new PropertyDescriptor ("created", Date.class);
            grid = new PropertyDescriptor ("grid", GameGrid.class);
            type = new PropertyDescriptor ("type", String.class);

        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return new PropertyDescriptor[] {id, rows, columns, players, created, state, grid, type };
    }
}
