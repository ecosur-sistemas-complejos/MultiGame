import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.mock.jms.MockTopic;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesPlayer;
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
    * Needs a rewrite. Removed from test cycle pending rewrite.
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
}
