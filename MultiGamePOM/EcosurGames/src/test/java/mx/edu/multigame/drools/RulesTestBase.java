/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Base class for rules based tests.
 * 
 * @author max@alwayssunny.com
 */

package mx.edu.multigame.drools;

import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.mock.jms.MockTopic;
import mx.ecosur.multigame.impl.model.GridCell;

public class RulesTestBase extends JMSTestCaseAdapter {

	protected  EJBTestModule ejbModule;

	protected  MockTopic mockTopic;

	protected void setUp() throws Exception {
		super.setUp();
		
		/* Set up mock JMS destination for message sender */
		ejbModule = createEJBTestModule();
		ejbModule.bindToContext("jms/TopicConnectionFactory",
				getJMSMockObjectFactory().getMockTopicConnectionFactory());
		//TODO: Externalize and change jndi name of topic
		mockTopic = getDestinationManager().createTopic("MultiGame");
		ejbModule.bindToContext("MultiGame", mockTopic);
	}

    protected void setIds(GridCell... cells) {
		int counter = 1;
		for (GridCell cell : cells) {
			cell.setId(counter++);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
