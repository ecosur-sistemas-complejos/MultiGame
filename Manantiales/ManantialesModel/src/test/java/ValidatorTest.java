import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.mock.jms.MockTopic;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.manantiales.entity.ManantialesFicha;
import mx.ecosur.multigame.manantiales.entity.ManantialesGame;
import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
import mx.ecosur.multigame.manantiales.enums.TokenType;
import mx.ecosur.multigame.manantiales.util.MovesValidator;
import mx.ecosur.multigame.model.interfaces.Move;
import org.junit.Before;
import org.junit.Test;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A unit test for the MovesValidator class.
 */
public class ValidatorTest extends JMSTestCaseAdapter {

    private ManantialesGame game;

    private ManantialesPlayer alice, bob, charlie, denise;

    private MockTopic mockTopic;

    private EJBTestModule ejbModule;

    /* Note: X and Y are reversed, so x is column, and y is row */
    private Point[] yellow = { new Point(0,4), new Point(1,4), new Point(2,4), new Point(3,4) };
    private Point[] purple = { new Point(4,5), new Point(4,6), new Point(4,7), new Point(4,8)};
    private Point[] red = { new Point(4,3), new Point(4,2), new Point(4,1), new Point(4,0)};
    private Point[] black = { new Point(5,4), new Point(6,4), new Point(7,4), new Point(8,4)};

    /* Scripted bad locations */
    private ManantialesFicha [] badLocations = { new ManantialesFicha(1,5, Color.PURPLE, TokenType.INTENSIVE_PASTURE),
        new ManantialesFicha(3,1,Color.YELLOW, TokenType.INTENSIVE_PASTURE), new ManantialesFicha(5,1,Color.RED,TokenType.INTENSIVE_PASTURE),
        new ManantialesFicha(7,3,Color.RED,TokenType.INTENSIVE_PASTURE), new ManantialesFicha(7,5,Color.BLACK, TokenType.INTENSIVE_PASTURE),
        new ManantialesFicha(1,3,Color.YELLOW,TokenType.INTENSIVE_PASTURE), new ManantialesFicha(3,7,Color.PURPLE,TokenType.INTENSIVE_PASTURE),
        new ManantialesFicha(5,7,Color.BLACK,TokenType.INTENSIVE_PASTURE)};

    @Before
    public void setUp() throws InvalidRegistrationException, Exception {
        super.setUp();

        /* Set up mock JMS destination for message sender */
        ejbModule = createEJBTestModule();
        ejbModule.bindToContext("MultiGameConnectionFactory",
                getJMSMockObjectFactory().getMockTopicConnectionFactory());
        mockTopic = getDestinationManager().createTopic("MultiGame");
        ejbModule.bindToContext("MultiGame", mockTopic);

        game = new ManantialesGame();
        GridRegistrant[] players = {
            new GridRegistrant ("alice"),
            new GridRegistrant ("bob"),
            new GridRegistrant ("charlie"),
            new GridRegistrant ("denise") 
        };

        /* Register our test players with the game */
        int counter = 0;
        for (int i = 0; i < players.length; i++) {
            game.registerPlayer (players [ counter++ ]);
        }

        /* Set the fields of the test */
        for (GridPlayer player : game.getPlayers()) {
            if (player.getName().equals("alice")) {
                    alice = (ManantialesPlayer) player;
            } else if (player.getName().equals("bob")) {
                    bob = (ManantialesPlayer) player;
            } else if (player.getName().equals("charlie")) {
                    charlie = (ManantialesPlayer) player;
            } else if (player.getName().equals("denise")) {
                    denise = (ManantialesPlayer) player;
            }
        }

        /* Lay in the board configuration */
        for (Point p : yellow) {
            if (p.x != 0 && p.x != 2)
                game.getGrid().updateCell(new ManantialesFicha(p.x,p.y, Color.YELLOW, TokenType.MANAGED_FOREST));
            else
                game.getGrid().updateCell(new ManantialesFicha(p.x,p.y, Color.YELLOW,TokenType.INTENSIVE_PASTURE));
        }

        for (Point p : red) {
            if (p.y != 2 && p.y != 0)
                game.getGrid().updateCell(new ManantialesFicha(p.x,p.y, Color.RED, TokenType.MANAGED_FOREST));
            else
                game.getGrid().updateCell(new ManantialesFicha(p.x,p.y, Color.RED,TokenType.INTENSIVE_PASTURE));
        }

        for (Point p : purple) {
            if (p.y != 6 && p.y != 8)
                game.getGrid().updateCell(new ManantialesFicha(p.x,p.y, Color.PURPLE, TokenType.MANAGED_FOREST));
            else
                game.getGrid().updateCell(new ManantialesFicha(p.x,p.y, Color.PURPLE,TokenType.INTENSIVE_PASTURE));
        }

        for (Point p : black) {
            if (p.x != 6 && p.x != 8)
                game.getGrid().updateCell(new ManantialesFicha(p.x,p.y, Color.BLACK, TokenType.MANAGED_FOREST));
            else
                game.getGrid().updateCell(new ManantialesFicha(p.x,p.y, Color.BLACK,TokenType.INTENSIVE_PASTURE));
        }

        for (ManantialesFicha f : badLocations) {
            /* Set the badlocation to a "moderate" ranch token */
            f.setType(TokenType.MODERATE_PASTURE);
            game.getGrid().updateCell(f);
        }

    }

    @Test
    public void testBadMoves() throws Exception {
       List<Move> badMoves = new ArrayList<Move>();
       for (ManantialesFicha f : badLocations) {
          ManantialesMove next = new ManantialesMove();
          switch (f.getColor()) {
              case YELLOW:
                  next.setPlayer(alice);
                  next.setCurrentCell(game.getGrid().getLocation(new GridCell(f.getColumn(), f.getRow(), f.getColor())));
                  next.setDestinationCell(f);
                  badMoves.add(next);
                  break;
              case PURPLE:
                  next.setPlayer(bob);
                  next.setCurrentCell(game.getGrid().getLocation(new GridCell(f.getColumn(),  f.getRow(), f.getColor())));
                  next.setDestinationCell(f);
                  badMoves.add(next);
                  break;
              case RED:
                  next.setPlayer(charlie);
                  next.setCurrentCell(game.getGrid().getLocation(new GridCell(f.getColumn(),  f.getRow(), f.getColor())));
                  next.setDestinationCell(f);
                  badMoves.add(next);
                  break;
              case BLACK:
                  next.setPlayer(denise);
                  next.setCurrentCell(game.getGrid().getLocation(new GridCell(f.getColumn(), f.getRow(), f.getColor())));
                  next.setDestinationCell(f);
                  badMoves.add(next);
                  break;
              default:
                  break;
          }
       }
        /* Assert assumptions are correct */
        assertEquals(8, badMoves.size());
        MovesValidator v = new MovesValidator(game, badMoves);
        v.visit();
        /* Assert validator is working */
        assertTrue(v.getMoves().size() == 0);
    }
}
