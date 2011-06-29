//copyright

package mx.ecosur.multigame.impl.entity.manantiales.beaninfo;

import mx.ecosur.multigame.impl.entity.manantiales.SimpleAgent;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author awaterma@ecosur.mx
 */
public class SimpleAgentInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor (SimpleAgent.class);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor type = null;

        /* ManantialesPlayer types */
        PropertyDescriptor score = null, forested = null, moderate = null, intensive = null, vivero = null,
                silvo = null;

        try {
            type = new PropertyDescriptor ("type", String.class);
            score = new PropertyDescriptor ("score", Integer.class);
            forested = new PropertyDescriptor ("forested", Integer.class);
            moderate = new PropertyDescriptor ("moderate", Integer.class);
            intensive = new PropertyDescriptor ("intensive", Integer.class);
            vivero = new PropertyDescriptor ("vivero", Integer.class);
            silvo = new PropertyDescriptor ("silvo", Integer.class);            

        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return new PropertyDescriptor[] { type, score, forested, moderate, intensive, vivero, silvo};
    }
}
