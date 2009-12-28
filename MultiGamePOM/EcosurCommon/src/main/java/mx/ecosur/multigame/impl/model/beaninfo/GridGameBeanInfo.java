/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
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

import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridGame;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.util.List;
import java.util.Date;

import org.drools.KnowledgeBase;

/**
 * Created by IntelliJ IDEA.
 * User: awater
 * Date: 22-oct-2009
 * Time: 10:13:45
 * To change this template use File | Settings | File Templates.
 */
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        PropertyDescriptor[] ret = {id, rows, columns, players, created, state, grid, type };
        return ret;
    }
}
