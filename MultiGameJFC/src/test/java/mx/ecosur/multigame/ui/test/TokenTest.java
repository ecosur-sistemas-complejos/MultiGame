/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

package mx.ecosur.multigame.ui.test;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.SharedBoard;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
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
		board.setGame(game);
		
		Player a = test.createPlayer("alice");
		Player b = test.createPlayer ("bob");
		
		GamePlayer alice = new GamePlayer (game,a, Color.BLACK);
		GamePlayer bob = new GamePlayer (game, b, Color.RED);
		
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
	
	private Player createPlayer(String name) {
		Player player = new Player ();
		player.setName(name);
		player.setId(++id);
		player.setLastRegistration(System.currentTimeMillis());
		player.setGamecount(0);
		player.setWins(0);
		return player;
	}
}
