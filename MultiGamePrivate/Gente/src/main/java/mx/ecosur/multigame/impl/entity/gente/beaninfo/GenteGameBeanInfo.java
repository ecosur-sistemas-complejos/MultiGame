/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl.entity.gente.beaninfo;

import mx.ecosur.multigame.impl.entity.gente.GenteGame;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.util.Set;

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
            e.printStackTrace();
        }

        return new PropertyDescriptor [] { winners };
    }
}
