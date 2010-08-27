package mx.ecosur.multigame.session;

import com.sun.appserv.security.ProgrammaticLogin;
import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.entity.manantiales.*;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.Agent;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.Move;
import mx.ecosur.multigame.model.Registrant;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author awaterma@ecosur.mx
 */
public class ManantialesAgencyTest {

    private RegistrarRemote registrar;

    private SharedBoardRemote board;

    private int gameId;

    private ManantialesPlayer alice;

    private SimpleAgent[] agents;


    @Before
    public void fixtures () throws RemoteException, NamingException, InvalidRegistrationException {
        ProgrammaticLogin login = new ProgrammaticLogin();
        login.login("MultiGame", "test");
        InitialContext ic = new InitialContext();

        agents = new SimpleAgent [3];
        
        registrar = (RegistrarRemote) ic.lookup(
            "mx.ecosur.multigame.ejb.interfaces.RegistrarRemote");

        ManantialesGame game = new ManantialesGame ();
        game.setMode(Mode.BASIC_PUZZLE);
        Game boardGame = new Game (game);

        Registrant registrant = registrar.register(new Registrant (new GridRegistrant ("alice")));
        boardGame = registrar.registerPlayer(boardGame, registrant);

        if (gameId == 0) {
            gameId = boardGame.getId();
        }

        Color[] colors = { Color.BLUE, Color.RED, Color.PURPLE };
        for (int i = 0; i < 3; i++) {
            registrar.registerAgent(boardGame, new Agent(new SimpleAgent(new GridRegistrant ("Agent-" + (i + 1)),
                    colors[i], AgentType.SIMPLE)));
        }

        /* Get the SharedBoard */
        board = (SharedBoardRemote) ic.lookup(
                "mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote");
        game = (ManantialesGame) board.getGame(gameId).getImplementation();

        /* Set the GamePlayers from the SharedBoard */
        List<GridPlayer> players = game.getPlayers();
        int counter = 0;
        for (GridPlayer p : players) {
            if (p.getRegistrant().getName().equals("alice"))
                alice = (ManantialesPlayer) p;
            else if (p instanceof SimpleAgent) {
                agents [ counter++ ] = (SimpleAgent) p;
            }
        }

        assertNotNull ("Alice not found in game!", alice);
        assertEquals (agents.length, 3);
    }

    @Test
       public void testBasicAgentMoves () throws InvalidMoveException, JMSException, InterruptedException {
           alice.setTurn (true);

           ManantialesFicha play = new ManantialesFicha(5, 4, alice.getColor(),
                           TokenType.MODERATE_PASTURE);

           ManantialesMove move = new ManantialesMove (alice, play);
           Game game = board.getGame(gameId);
           ManantialesGame currentGame = (ManantialesGame) game.getImplementation();
           Move model = board.doMove(game, new Move(move));
           move = (ManantialesMove) model.getImplementation();

           game = board.getGame(gameId);
           currentGame = (ManantialesGame) game.getImplementation();
           GameGrid grid = currentGame.getGrid();

           assertEquals (MoveStatus.EVALUATED, move.getStatus());
           assertEquals (play, grid.getLocation(play));

           /* Wait for agents to move */
           Thread.sleep(15000);

           game = board.getGame(gameId);
           currentGame = (ManantialesGame) game.getImplementation();
           grid = currentGame.getGrid();

           assertEquals (4, grid.getCells().size());
       }

}
