/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Test class for JMS bean "StrategyPlayerListener".
 * 
 * @author awaterma@ecosur.mx
 */

package mx.edu.multigame.drools;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.jms.pente.StrategyPlayerListener;

import org.mockejb.MDBDescriptor;
import org.mockejb.MockContainer;

import com.mockrunner.mock.ejb.EJBMockObjectFactory;


public class AgentListenerTest extends AgentTestBase {
	
	
	private MockContainer mockContainer;
	    
    @Override
    public void setUp() throws Exception {
    	super.setUp();
    	
    	EJBMockObjectFactory factory = createEJBMockObjectFactory();
    	mockContainer = factory.getMockContainer();
 
        MDBDescriptor jmsPlayerListenerMDBDescriptor = 
            new MDBDescriptor( "jms/TopicConnectionFactory", "CHECKERS", new StrategyPlayerListener ());
	    jmsPlayerListenerMDBDescriptor.setIsTopic(true);
	    
	    /* Deploy the MDB */
	    mockContainer.deploy(jmsPlayerListenerMDBDescriptor);
    }
    
    public void testPlayerListener () {
    	MessageSender messageSender = new MessageSender();
    	int existing = game.getGrid().getCells().size();
    	messageSender.sendPlayerChange(game);
    	//assertEquals ("Alice did not move!", existing + 1, game.getGrid().getCells().size());
    }
}
