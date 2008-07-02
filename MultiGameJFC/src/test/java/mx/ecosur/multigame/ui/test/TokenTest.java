package mx.ecosur.multigame.ui.test;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.SharedBoard;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ui.GameFrame;


public class TokenTest  {
	
	int id;
	
	public TokenTest () {
		id = 0;
	}

	
	public static void main (String[] args) throws Exception {
		TokenTest test = new TokenTest ();
		SharedBoard board = new SharedBoard ();
		Game game = new Game();
		game.initialize(GameType.CHECKERS);
		
		Player alice = test.createPlayer("alice", Color.BLACK);
		Player bob = test.createPlayer ("bob", Color.RED);
		game.addPlayer(alice);
		game.addPlayer(bob);
		
		/* Initialize the board */
		game.setState(GameState.BEGIN);
		board.setGame(game);
		
		/* Create the ui */
		GameFrame frame = new GameFrame (
				"Multi-Game", board, bob);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	private Player createPlayer(String name, Color color) {
		Player player = new Player ();
		player.setName(name);
		player.setColor(color);
		player.setId(++id);
		player.setLastRegistration(System.currentTimeMillis());
		player.setGamecount(0);
		player.setWins(0);
		return player;
	}
}
