/**
 * 
 */
package mx.edu.multigame.drools;

import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.mock.jms.MockTopic;

/**
 * Base class for rules based tests.
 */
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
		mockTopic = getDestinationManager().createTopic("CHECKERS");
		ejbModule.bindToContext("CHECKERS", mockTopic);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
}
