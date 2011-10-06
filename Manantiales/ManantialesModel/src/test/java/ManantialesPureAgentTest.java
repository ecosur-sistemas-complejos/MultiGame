import java.util.Random;

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.DummyMessageSender;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.entity.manantiales.SimpleAgent;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.model.interfaces.Move;

import org.junit.Before;
import org.junit.Test;

import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.mock.jms.MockTopic;

import static org.junit.Assert.*;

public class ManantialesPureAgentTest extends JMSTestCaseAdapter {

    private ManantialesGame game;
    private SimpleAgent[] agents;
    private MockTopic mockTopic;
    private EJBTestModule ejbModule;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        /* Set up mock JMS destination for message sender */
        ejbModule = createEJBTestModule();
        ejbModule.bindToContext("MultiGameConnectionFactory",
                getJMSMockObjectFactory().getMockTopicConnectionFactory());
        mockTopic = getDestinationManager().createTopic("MultiGame");
        ejbModule.bindToContext("MultiGame", mockTopic);
        
        game = new ManantialesGame();
        game.setMessageSender(new DummyMessageSender());
        agents = new SimpleAgent [ 4 ];

        Color [] colors = { Color.YELLOW, Color.BLUE, Color.RED, Color.PURPLE };

        for (int i = 0; i < agents.length; i++) {
            GridRegistrant registrant = new GridRegistrant ("Agent-" + (i + 1));
            agents [ i ] = (SimpleAgent) game.registerAgent(new SimpleAgent(registrant, colors[i], AgentType.SIMPLE));
        }

        game.setMode(Mode.CLASSIC);
    }
    
    @Test
    public void testEmptyMoves () throws InvalidMoveException {
        int counter = 1;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < agents.length; j++) {
                Move move = agents [ j ].generatePassMove(game);
                move.setId(counter);
                move = game.move(move);
                assertEquals(MoveStatus.EVALUATED, move.getStatus());
                counter++;
            }
        }
        assertEquals(16, game.getMoves().size());
    }
    
/*    public void testMixedMoves() throws InvalidMoveException {
        int counter = 1;
        Random rnd = new Random();
        Move move = null;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < agents.length; j++) {
                move = agents [ j ].determineMoves(game).iterator().next();
                move.setId(counter);
                game.move(move);
                assertEquals(MoveStatus.EVALUATED, move.getStatus());
                counter++;
            }
        }
        
        assertEquals (32, game.getMoves().size());
    }*/
}
