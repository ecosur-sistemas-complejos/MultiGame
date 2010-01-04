/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 *
 * GenteGameBeanInfo 
 *
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.gente.beaninfo;

import mx.ecosur.multigame.impl.entity.gente.GenteGame;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: awater
 * Date: 22-oct-2009
 * Time: 11:06:38
 * To change this template use File | Settings | File Templates.
 */
public class GenteGameBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor (GenteGame.class);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor winners = null;

        try {
            winners = new PropertyDescriptor ("winners", Set.class);
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return new PropertyDescriptor [] { winners };
    }
}
