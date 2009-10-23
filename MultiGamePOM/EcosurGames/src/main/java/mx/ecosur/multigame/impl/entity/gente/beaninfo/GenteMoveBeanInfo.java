/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 *
 * GenteMoveBeanInfo 
 *
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.gente.beaninfo;

import mx.ecosur.multigame.impl.entity.gente.GenteMove;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: awater
 * Date: 22-oct-2009
 * Time: 11:13:54
 * To change this template use File | Settings | File Templates.
 */
public class GenteMoveBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor (GenteMove.class);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor trias = null, tesseras = null, teamColors = null, searchCount = null,
                qualifier = null;

        try {
            trias = new PropertyDescriptor ("trias", HashSet.class);
            tesseras = new PropertyDescriptor ("tesseras", HashSet.class);
            teamColors = new PropertyDescriptor ("teamColors", ArrayList.class);
            searchCount = new PropertyDescriptor ("searchCount", Integer.class);
            qualifier = new PropertyDescriptor ("qualifier", String.class);
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return new PropertyDescriptor[] { trias, tesseras, teamColors, searchCount, qualifier };
    }

}
