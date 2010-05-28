/**
 * @author awaterma@ecosur.mx
 */

import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.mock.jms.MockTopic;
import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.DummyMessageSender;
import mx.ecosur.multigame.impl.entity.manantiales.*;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.List;

public class ManantialesAgentTest extends JMSTestCaseAdapter {

    private ManantialesGame game;

    private ManantialesPlayer alice;

    private SimpleAgent[] agents;

    private MockTopic mockTopic;

    private EJBTestModule ejbModule;

    private static int lastId;

    private static KnowledgeBase manantiales;


    /* Setup manantiales kbase */
    static {
        manantiales = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(ManantialesGame.class.getResourceAsStream (
            "/mx/ecosur/multigame/impl/manantiales.xml")), ResourceType.CHANGE_SET);
        manantiales.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        /* Set up mock JMS destination for message sender */
        ejbModule = createEJBTestModule();
        ejbModule.bindToContext("jms/TopicConnectionFactory",
                getJMSMockObjectFactory().getMockTopicConnectionFactory());
        mockTopic = getDestinationManager().createTopic("MultiGame");
        ejbModule.bindToContext("MultiGame", mockTopic);

        game = new ManantialesGame(manantiales);
        game.setMessageSender(new DummyMessageSender());

        GridRegistrant registrant = new GridRegistrant ("alice");
        alice = (ManantialesPlayer) game.registerPlayer(registrant);
        agents = new SimpleAgent [ 3 ];
        registrant = null;
        Color [] colors = { Color.BLUE, Color.RED, Color.PURPLE };

        for (int i = 0; i < 3; i++) {
            registrant = new GridRegistrant ("Agent-" + (i + 1));
            agents [ i ] = (SimpleAgent) game.registerAgent(new SimpleAgent(registrant, colors[i], AgentType.SIMPLE));
        }
    }


    public static void setIds (GridCell... cells) {
        for (GridCell cell : cells) {
            cell.setId(++lastId);
        }
    }

    @Test
    public void testExecuteMove () throws InvalidMoveException {
        alice.setTurn (true);
        game.setState(GameState.PLAY);

        Ficha play = new Ficha (5, 4, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        setIds(play);
        ManantialesMove move = new ManantialesMove (alice, play);
        game.move (move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (play, game.getGrid().getLocation(play));

        /* test the scoring */
        assertEquals (1, alice.getModerate());
        assertEquals (2, alice.getScore());
    }

     @Test
    public void testBasicAgentMoves () throws InvalidMoveException, JMSException {
        alice.setTurn (true);
        game.setState(GameState.PLAY);

        Ficha play = new Ficha (3,3, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        setIds(play);
        ManantialesMove move = new ManantialesMove (alice, play);
        game.move (move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (play, game.getGrid().getLocation(play));

        /* test the scoring */
        assertEquals (1, alice.getModerate());
        assertEquals (2, alice.getScore());
        assertFalse (alice.isTurn());

        for (int i = 0; i < agents.length; i++) {
            agents [ i ].setTurn(true);
            ManantialesMove agentMove = (ManantialesMove) agents [ i ].determineNextMove(game);
            assertNotNull (agentMove.getDestinationCell());
            game.move (agentMove);
            assertEquals (MoveStatus.EVALUATED, agentMove.getStatus());
            GridCell destination = agentMove.getDestinationCell();
            assertEquals (destination, game.getGrid().getLocation(destination));
            List<Message> messages = mockTopic.getCurrentMessageList();
            boolean found = false;
            for (Message message : messages) {
                ObjectMessage msg = (ObjectMessage) message;
                if (message.getStringProperty("GAME_EVENT").equals(GameEvent.MOVE_COMPLETE.name())) {
                    ManantialesMove test = (ManantialesMove) msg.getObject();
                    if (test.getPlayer().equals (agents [ i ])) {
                           found = true;
                           assertEquals (agentMove, test);
                    }
                }
            }

            assertTrue (found);
        }

        for (GridCell cell : game.getGrid().getCells()) {
            Ficha ficha = (Ficha) cell;
            assertTrue ("Location is incorrect! " + ficha, isGoodLocation (ficha));
        }

        assertEquals (4, game.getGrid().getCells().size());         
    }

    private boolean isGoodLocation (Ficha ficha) {
        boolean ret;

        int column = ficha.getColumn();
        int row = ficha.getRow();
        Color color = ficha.getColor();

        switch (color) {
            case YELLOW:
                ret = (column < 5 && row < 5);
                break;
            case PURPLE:
                ret = (column < 5 && row > 3);
                break;
            case RED:
                ret = (column > 3 && row < 5);
                break;
            case BLACK:
                ret = (column > 3 && row > 3);
                break;
            default:
                ret = false;
        }

        /* Check for Manantial */
        ret = ret & (column !=4 && row != 4);
        return ret;
    }

}
