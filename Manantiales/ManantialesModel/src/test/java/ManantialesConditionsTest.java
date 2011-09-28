import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.mock.jms.MockTopic;

import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.impl.entity.manantiales.CheckCondition;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesPlayer;
import mx.ecosur.multigame.impl.enums.manantiales.ConditionType;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.model.interfaces.Move;
import org.junit.Before;
import org.junit.Test;

import static util.TestUtilities.*;

import com.mockrunner.jms.JMSTestCaseAdapter;

public class ManantialesConditionsTest extends JMSTestCaseAdapter {

    private ManantialesGame game;

    private ManantialesPlayer alice, bob, charlie, denise;

    private MockTopic mockTopic;

    private EJBTestModule ejbModule;

    private List<Message> filterForEvent (GameEvent event) throws JMSException {
        ArrayList ret = new ArrayList();
        List<Message> ml = mockTopic.getReceivedMessageList();
        for (Message msg : ml) {
            if (msg.getStringProperty("GAME_EVENT").equals(event.name()))
                ret.add(msg);
        }
        
        return ret;
    }
    

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
        alice = (ManantialesPlayer) game.registerPlayer(new GridRegistrant("alice"));
        bob = (ManantialesPlayer) game.registerPlayer(new GridRegistrant("bob"));
        charlie = (ManantialesPlayer) game.registerPlayer(new GridRegistrant("charlie"));
        denise = (ManantialesPlayer) game.registerPlayer(new GridRegistrant("denise"));
    }


    /** Test on ManantialesGame for setting check constraints
    *
    * @throws InvalidMoveException */
   @Test
   public void testManantialCheckConstraints () throws InvalidMoveException {
       ManantialesFicha ficha = new ManantialesFicha(4,3, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha2 = new ManantialesFicha(4,5, bob.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha3 = new ManantialesFicha(3,4, charlie.getColor(),
               TokenType.MODERATE_PASTURE);
       SetIds(ficha, ficha2, ficha3);
       ManantialesMove move = new ManantialesMove (alice, ficha);
       move.setMode(game.getMode());
       Move mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       bob.setTurn(true);
       move = new ManantialesMove (bob, ficha2);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       charlie.setTurn(true);
       move = new ManantialesMove (charlie, ficha3);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       assertEquals(3, game.getMoves().size());
       assertEquals(1, game.getCheckConditions().size());
   }
   
   @Test
   public void testManantialesCheckConstriantsRelieved () throws InvalidMoveException, JMSException {
       ManantialesFicha ficha = new ManantialesFicha(4,3, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha2 = new ManantialesFicha(4,5, bob.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha3 = new ManantialesFicha(3,4, charlie.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha relief = new ManantialesFicha(4,3, alice.getColor(),
               TokenType.MANAGED_FOREST);
       ManantialesFicha retrigger = new ManantialesFicha (4,3, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha terminate = new ManantialesFicha (5,5,denise.getColor(),
               TokenType.MODERATE_PASTURE);
       SetIds(ficha, ficha2, ficha3, retrigger, terminate);
       ManantialesMove move = new ManantialesMove (alice, ficha);
       move.setMode(game.getMode());
       Move mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       bob.setTurn(true);
       move = new ManantialesMove (bob, ficha2);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       charlie.setTurn(true);
       move = new ManantialesMove (charlie, ficha3);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       List filtered = filterForEvent(GameEvent.CONDITION_RAISED);
       assertTrue(filtered.size() == 1);
       alice.setTurn(true);
       move = new ManantialesMove (alice, ficha, relief);
       game.move(move);
       filtered = filterForEvent(GameEvent.CONDITION_RESOLVED);
       assertTrue(filtered.size () == 1);
       filtered = filterForEvent(GameEvent.CONDITION_TRIGGERED);
       assertTrue(filtered.size() == 0);
       
       /* retrigger condition */
       alice.setTurn(true);
       move = new ManantialesMove(alice, relief, retrigger);
       game.move(move);
       filtered = filterForEvent(GameEvent.CONDITION_RAISED);
       assertTrue("filter size is: " + filtered.size(), filtered.size() == 2);
       /* allow condition to terminate game, i.e., TRIGGER */
       denise.setTurn(true);
       move = new ManantialesMove (denise, terminate);
       game.move(move);
       filtered = filterForEvent(GameEvent.CONDITION_TRIGGERED);
       assertTrue(filtered.size() == 1);
       assertTrue(game.getState().equals(GameState.ENDED));
  }

  @Test
   public void testWestCheckConstraints () throws InvalidMoveException {
       ManantialesFicha ficha = new ManantialesFicha(2,4, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha2 = new ManantialesFicha(1,4, bob.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha3 = new ManantialesFicha(0,4, charlie.getColor(),
               TokenType.MODERATE_PASTURE);
       SetIds(ficha, ficha2, ficha3);
      ManantialesMove move = new ManantialesMove (alice, ficha);
      Move mv = game.move(move);
      assertEquals(MoveStatus.EVALUATED, mv.getStatus());
      bob.setTurn(true);
      move = new ManantialesMove (bob, ficha2);
      mv = game.move(move);
      assertEquals(MoveStatus.EVALUATED, mv.getStatus());
      charlie.setTurn(true);
      move = new ManantialesMove (charlie, ficha3);
      mv = game.move(move);
      assertEquals(MoveStatus.EVALUATED, mv.getStatus());
      assertEquals (3, game.getMoves().size());
      assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
      assertEquals(1, game.getCheckConditions().size());
   }

   @Test
   public void testNorthCheckConstraints () throws InvalidMoveException {
       ManantialesFicha ficha = new ManantialesFicha(4,0, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha2 = new ManantialesFicha(4,1, bob.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha3 = new ManantialesFicha(4,2, charlie.getColor(),
               TokenType.MODERATE_PASTURE);
       SetIds(ficha, ficha2, ficha3);
       ManantialesMove move = new ManantialesMove (alice, ficha);
       Move mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       bob.setTurn(true);
       move = new ManantialesMove (bob, ficha2);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       charlie.setTurn(true);
       move = new ManantialesMove (charlie, ficha3);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       assertEquals (3, game.getMoves().size());
       assertTrue("CheckConstraint not fired!", game.getCheckConditions() != null);
       assertEquals(1, game.getCheckConditions().size());
   }

   @Test
   public void testEastCheckConstraints () throws InvalidMoveException {
       ManantialesFicha ficha = new ManantialesFicha(6,4, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha2 = new ManantialesFicha(7,4, bob.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha3 = new ManantialesFicha(8,4, charlie.getColor(),
               TokenType.MODERATE_PASTURE);
       SetIds(ficha, ficha2, ficha3);
       ManantialesMove move = new ManantialesMove (alice, ficha);
       Move mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       bob.setTurn(true);
       move = new ManantialesMove (bob, ficha2);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       charlie.setTurn(true);
       move = new ManantialesMove (charlie, ficha3);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       assertEquals (3, game.getMoves().size());
       assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
       assertEquals (1, game.getCheckConditions().size());
   }

   @Test
   public void testSouthCheckConstraints () throws InvalidMoveException {
       ManantialesFicha ficha = new ManantialesFicha(4,6, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha2 = new ManantialesFicha(4,7, bob.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha ficha3 = new ManantialesFicha(4,8, charlie.getColor(),
               TokenType.MODERATE_PASTURE);
       SetIds(ficha, ficha2, ficha3);
       ManantialesMove move = new ManantialesMove (alice, ficha);
       Move mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       bob.setTurn(true);
       move = new ManantialesMove (bob, ficha2);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       charlie.setTurn(true);
       move = new ManantialesMove (charlie, ficha3);
       mv = game.move(move);
       assertEquals(MoveStatus.EVALUATED, mv.getStatus());
       assertEquals (3, game.getMoves().size());
       assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
       assertEquals(1, game.getCheckConditions().size());
   }
   
   /* Tests for a constraint triggered by red, and ensure that it will expire 
    * Bug seen in UI.*/
   @Test
   public void testYellowYellowRed() throws InvalidMoveException, JMSException {
       ManantialesFicha a1 = new ManantialesFicha (3,4, alice.getColor(),
                   TokenType.MODERATE_PASTURE);
       ManantialesFicha a2 = new ManantialesFicha (2,4, alice.getColor(), 
               TokenType.MODERATE_PASTURE);
       ManantialesFicha trigger = new ManantialesFicha (0,4, charlie.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha sp1 = new ManantialesFicha (5,7, denise.getColor(),
               TokenType.MANAGED_FOREST);
       ManantialesFicha sp2 = new ManantialesFicha (1,1, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha expiry = new ManantialesFicha (6,0, bob.getColor(),
               TokenType.MANAGED_FOREST);
       SetIds(a1,a2,trigger,sp1, sp2, expiry);
       ManantialesMove move = new ManantialesMove(alice, a1);
       game.move(move);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       alice.setTurn(true);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       move = new ManantialesMove(alice, a2);
       game.move(move);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       charlie.setTurn(true);
       move = new ManantialesMove(charlie, trigger);
       game.move(move);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       List<Message> messages = filterForEvent(GameEvent.CONDITION_RAISED);
       assertEquals(1, messages.size());
       move = new ManantialesMove(denise, sp1);
       game.move(move);
       move = new ManantialesMove(alice, sp2);
       game.move(move);
       move = new ManantialesMove(bob, expiry);
       game.move(move);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       messages = filterForEvent(GameEvent.CONDITION_TRIGGERED);
       assertEquals(1, messages.size());
   }
   
   /* Tests for a constraint triggered by red, and ensure that it will expire 
    * Bug seen in UI.*/
   @Test
   public void testYellowYellowRedWithPassMove() throws InvalidMoveException, JMSException {
       ManantialesFicha a1 = new ManantialesFicha (3,4, alice.getColor(),
                   TokenType.MODERATE_PASTURE);
       ManantialesFicha a2 = new ManantialesFicha (2,4, alice.getColor(), 
               TokenType.MODERATE_PASTURE);
       ManantialesFicha trigger = new ManantialesFicha (0,4, charlie.getColor(),
               TokenType.MODERATE_PASTURE);
       ManantialesFicha sp1 = new ManantialesFicha (5,7, denise.getColor(),
               TokenType.MANAGED_FOREST);
       ManantialesFicha sp2 = new ManantialesFicha (1,1, alice.getColor(),
               TokenType.MODERATE_PASTURE);
       SetIds(a1,a2,trigger,sp1, sp2);
       ManantialesMove move = new ManantialesMove(alice, a1);
       game.move(move);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       alice.setTurn(true);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       move = new ManantialesMove(alice, a2);
       game.move(move);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       charlie.setTurn(true);
       move = new ManantialesMove(charlie, trigger);
       game.move(move);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       List<Message> messages = filterForEvent(GameEvent.CONDITION_RAISED);
       assertEquals(1, messages.size());
       move = new ManantialesMove(denise, sp1);
       game.move(move);
       move = new ManantialesMove(alice, sp2);
       game.move(move);
       move = generatePassMove(bob);
       game.move(move);
       assertEquals(MoveStatus.EVALUATED, move.getStatus());
       messages = filterForEvent(GameEvent.CONDITION_TRIGGERED);
       assertEquals(1, messages.size());
   }
   
   private ManantialesMove generatePassMove(ManantialesPlayer p) {
       ManantialesMove ret = new ManantialesMove ();
       ret.setPlayer(p);
       ret.setBadYear(true);
       ret.setStatus(MoveStatus.UNVERIFIED);
       ret.setMode(game.getMode());
       return ret;   
   }
   
}
