import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteMove;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;
import mx.ecosur.multigame.impl.enums.gente.GenteStrategy;
import mx.ecosur.multigame.impl.model.GridMove;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.test.RulesTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Each type of agent plays out a full game against other agents of the same type.
 * This ensures that agents work properly in a full gaming context.
 *
 * Tests fail if an agent cannot move or any other exception is generated.
 *
 */
public class GenteAgentTest extends RulesTestBase {

    protected GridRegistrant a,b,c,d;

	protected GenteStrategyAgent alice, bob, charlie, denise;

	protected GenteGame game;

    private static final int Ticks = 500;

@Before
	public void setUp() throws Exception {
		super.setUp();
		game = new GenteGame();
		a = new GridRegistrant ("alice");
		b = new GridRegistrant ("bob");
		c = new GridRegistrant ("charlie");
		d = new GridRegistrant ("denise");
    }


    @Test
    public void testBlockerAgent () throws Exception {
        int counter = Ticks;
        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.BLOCKER);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.BLOCKER);
        charlie = new GenteStrategyAgent (c, Color.RED, GenteStrategy.BLOCKER);
        denise = new GenteStrategyAgent (d, Color.GREEN, GenteStrategy.BLOCKER);

        game.registerAgent(alice);
        game.registerAgent(bob);
        game.registerAgent(charlie);
        game.registerAgent(denise);

        while (game.getState().equals(GameState.PLAY) && counter > 0) {
            boolean moved = false;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    GenteStrategyAgent agent = (GenteStrategyAgent) player;
                    GenteMove move = agent.determineNextMove(game);
                    game.move(move);
                    assertEquals (MoveStatus.EVALUATED, move.getStatus());
                    moved = true;
                    System.out.println (Ticks - counter + ":" + move);
                 }
                counter--;
            }
            if (!moved)
                fail();
        }

    }

    @Test
    public void testSimpleAgent () throws Exception {
        int counter = Ticks;
        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.SIMPLE);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.SIMPLE);
        charlie = new GenteStrategyAgent (c, Color.RED, GenteStrategy.SIMPLE);
        denise = new GenteStrategyAgent (d, Color.GREEN, GenteStrategy.SIMPLE);

        game.registerAgent(alice);
        game.registerAgent(bob);
        game.registerAgent(charlie);
        game.registerAgent(denise);

        while (game.getState().equals(GameState.PLAY) && counter > 0) {
            boolean moved = false;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    GenteStrategyAgent agent = (GenteStrategyAgent) player;
                    GenteMove move = agent.determineNextMove(game);
                    game.move(move);
                    assertEquals (MoveStatus.EVALUATED, move.getStatus());
                    moved = true;
                    System.out.println (Ticks - counter + ":" + move);
                    counter--;
                 }
            }
            if (!moved)
                fail();
        }
    }

    public void testRandomAgent () throws Exception {
        int counter = Ticks;
        alice = new GenteStrategyAgent (a, Color.YELLOW, GenteStrategy.RANDOM);
        bob = new GenteStrategyAgent (b, Color.BLUE, GenteStrategy.RANDOM);
        charlie = new GenteStrategyAgent (c, Color.RED, GenteStrategy.RANDOM);
        denise = new GenteStrategyAgent (d, Color.GREEN, GenteStrategy.RANDOM);

        game.registerAgent(alice);
        game.registerAgent(bob);
        game.registerAgent(charlie);
        game.registerAgent(denise);

        while (game.getState().equals(GameState.PLAY) && counter > 0) {
            boolean moved = false;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    GenteStrategyAgent agent = (GenteStrategyAgent) player;
                    GenteMove move = agent.determineNextMove(game);
                    game.move(move);
                    assertEquals (MoveStatus.EVALUATED, move.getStatus());
                    moved = true;
                    System.out.println (Ticks - counter + ":" + move);
                    counter--;
                 }
            }
            if (!moved)
                fail();
        }

    }
}
