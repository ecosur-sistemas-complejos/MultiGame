/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
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
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return new PropertyDescriptor[] { trias, tesseras };
    }

}
