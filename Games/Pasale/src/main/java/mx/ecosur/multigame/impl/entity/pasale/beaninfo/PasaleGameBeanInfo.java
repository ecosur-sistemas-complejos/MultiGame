package mx.ecosur.multigame.impl.entity.pasale.beaninfo;

import mx.ecosur.multigame.impl.entity.pasale.PasaleGame;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGrid;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Collection;

/**
 * @author awaterma@ecosur.mx
 */
public class PasaleGameBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor (PasaleGame.class);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {

        /* PasaleGame types */
        PropertyDescriptor grid = null, maxPlayers = null, moves = null, version = null;

        try {
            grid = new PropertyDescriptor ("grid", PasaleGrid.class);
            maxPlayers = new PropertyDescriptor("maxPlayers", Integer.class);
            moves = new PropertyDescriptor ("moves", Collection.class);
            version = new PropertyDescriptor ("version", Integer.class); 

        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return new PropertyDescriptor[] { grid, maxPlayers, moves, version };
    }

}
