/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Test class for JMS bean "AgentListener".
 * 
 * @author awaterma@ecosur.mx
 */

package mx.edu.multigame.drools;

import javax.naming.Context;
import javax.naming.InitialContext;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;

import mx.ecosur.multigame.model.Game;

import org.mockejb.MDBDescriptor;
import org.mockejb.MockContainer;


public class GenteAgentListenerTest extends GenteAgentTestBase {
	
	
	private MockContainer mockContainer;
	
    private Context context;    

	    
    @Override
    public void setUp() throws Exception {
    	super.setUp();
 
    	context = new InitialContext();
    	mockContainer = new MockContainer (context);
 
        MDBDescriptor jmsPlayerListenerMDBDescriptor = 
            new MDBDescriptor( "jms/TopicConnectionFactory", 
            		"MultiGame", new GenteStrategyAgent ());
	    jmsPlayerListenerMDBDescriptor.setIsTopic(true);
	    
	    /* Deploy the MDB */
	    mockContainer.deploy(jmsPlayerListenerMDBDescriptor);
    }
    
    public void testPlayerListener () throws InvalidMoveException {
    	MessageSender sender = new MessageSender (context);
    	sender.sendPlayerChange(new Game(game));
    }
}
