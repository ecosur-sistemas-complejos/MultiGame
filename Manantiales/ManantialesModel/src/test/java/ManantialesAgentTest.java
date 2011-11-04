/**
 * @author awaterma@ecosur.mx
 */

import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.mock.jms.MockTopic;
import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.grid.DummyMessageSender;
import mx.ecosur.multigame.impl.entity.manantiales.*;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.model.interfaces.GamePlayer;
import mx.ecosur.multigame.model.interfaces.Move;
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

        GridRegistrant registrant = new GridRegistrant ("alice");
        alice = (ManantialesPlayer) game.registerPlayer(registrant);
        agents = new SimpleAgent [ 3 ];
        Color [] colors = { Color.PURPLE, Color.RED, Color.BLACK };

        for (int i = 0; i < 3; i++) {
            registrant = new GridRegistrant ("Agent-" + (i + 1));
            agents [ i ] = (SimpleAgent) game.registerAgent(new SimpleAgent(registrant, colors[i], AgentType.SIMPLE));
        }

        game.setMode(Mode.BASIC_PUZZLE);
    }


    public static void setIds (GridCell... cells) {
        for (GridCell cell : cells) {
            cell.setId(++lastId);
        }
    }

    @Test
    public void testExecuteMove () throws InvalidMoveException {
        ManantialesFicha play = new ManantialesFicha(5, 4, alice.getColor(),
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
        game.setMode(Mode.COMPETITIVE);
        ManantialesFicha play = new ManantialesFicha(3,3, alice.getColor(),
                        TokenType.MODERATE_PASTURE);
        setIds(play);
        ManantialesMove move = new ManantialesMove (alice, play);
        move.setMode(game.getMode());
        game.move (move);

        assertEquals (MoveStatus.EVALUATED, move.getStatus());
        assertEquals (play, game.getGrid().getLocation(play));

        /* test the scoring */
        assertEquals (1, alice.getModerate());
        assertEquals (2, alice.getScore());
        assertFalse (alice.isTurn());

        for (int i = 0; i < agents.length; i++) {
            if (agents [ i ].ready()) {
                List<Move> moves = agents [ i ].determineMoves(game);
                assertTrue ("Not enough moves (" + moves.size() + " moves) generated for Agent [" + agents [ i ] + "]", moves.size() > 0);
                for (Move agentMove : moves) {
                    ManantialesMove mve = (ManantialesMove) agentMove;
                    if (mve.isBadYear())
                        continue;
                    assertTrue (mve.getPlayer().isTurn());
                    assertNotNull (agentMove.getDestinationCell());
                    game.move (agentMove);
                    assertEquals (MoveStatus.EVALUATED, agentMove.getStatus());
                    GridCell destination = (GridCell) agentMove.getDestinationCell();
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
                    break;
                }
            }
        }

        for (GridCell cell : game.getGrid().getCells()) {
            ManantialesFicha ficha = (ManantialesFicha) cell;
            assertTrue ("Location is incorrect! " + ficha, isGoodLocation (ficha));
        }
    }

    /* A  test for confirming that the purple agent creates moves for both borders.
       Test for bug MG-20.
     */
    @Test
    public void testAgentPurple () {
        assertTrue(true);
        SimpleAgent purpleAgent = null;
        for (GamePlayer p : game.listPlayers()) {
            if (((ManantialesPlayer) p).getColor().equals(Color.PURPLE)) {
                SimpleAgent purple = (SimpleAgent) p;
                for (int i = 5; i < 9; i++) {
                    ManantialesFicha f = new ManantialesFicha (4,i, Color.PURPLE, TokenType.MANAGED_FOREST);
                    assertTrue(purple.isGoodLocation(f));
                }
            }
        }

    }

    private boolean isGoodLocation (ManantialesFicha ficha) {
        boolean ret;
        Color color = ficha.getColor();
        int column = ficha.getColumn(), row = ficha.getRow();

        switch (color) {
            case YELLOW:
                ret = (column <= 4 && row <= 4);
                break;
            case PURPLE:
                ret = (column <= 4 && row >= 4);
                break;
            case RED:
                ret = (column >= 4 && row <= 4);
                break;
            case BLACK:
                ret = (column >= 4 && row >= 4);
                break;
            default:
                ret = false;
        }

        if (column == 4 || row == 4) {
            /* Check for Manantial */
            if (column == 4 && row == 4)
                ret = false;
        } else {
            if (row % 2 == 0 && column % 2 == 0) {
                // even
                ret = ret && true;

            } else if (row % 2 != 0 && column % 2 != 0) {
                //odd
                ret = ret && true;
            } else
                ret = false;
        }

        return ret;
    }
    
    public void testEmptyMoves () {
        game.setMode(Mode.COMPETITIVE);
        ManantialesFicha ficha = new ManantialesFicha (2,0,alice.getColor(),
                TokenType.MANAGED_FOREST);
        
    }
}
