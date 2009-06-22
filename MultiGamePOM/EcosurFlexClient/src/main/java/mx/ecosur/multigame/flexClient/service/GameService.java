/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

package mx.ecosur.multigame.flexClient.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;


import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.flexClient.exception.GameException;
import mx.ecosur.multigame.impl.ejb.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.impl.ejb.entity.pente.PenteStrategyPlayer;
import mx.ecosur.multigame.impl.pente.PenteStrategy;
import mx.ecosur.multigame.GameType;
import flex.messaging.FlexContext;
import flex.messaging.FlexSession;

public class GameService {

	private static Logger logger = Logger.getLogger(GameService.class
			.getCanonicalName());

	private SharedBoardRemote getSharedBoard() {
		FlexSession session = FlexContext.getFlexSession();
		SharedBoardRemote sharedBoard = (SharedBoardRemote) session
				.getAttribute("sharedBoard");
		if (sharedBoard == null) {
			try {
				InitialContext ic = new InitialContext();
				sharedBoard = (SharedBoardRemote) ic
						.lookup(SharedBoardRemote.class.getName());
				session.setAttribute("sharedBoard", sharedBoard);
			} catch (NamingException e) {
				e.printStackTrace();
				throw new GameException(e);
			}
		}
		return sharedBoard;
	}

	private RegistrarRemote getRegistrar() {
		FlexSession session = FlexContext.getFlexSession();
		RegistrarRemote registrar = (RegistrarRemote) session
				.getAttribute("registrar");
		if (registrar == null) {
			try {
				InitialContext ic = new InitialContext();
				registrar = (RegistrarRemote) ic.lookup(RegistrarRemote.class
						.getName());
				session.setAttribute("registrar", registrar);
			} catch (NamingException e) {
				e.printStackTrace();
				throw new GameException(e);
			}
		}
		return registrar;
	}
	
	public GamePlayer startNewGame(Player player, Color preferedColor,
			String gameTypeStr) {
		GamePlayer gamePlayer = null;
		try {
			RegistrarRemote registrar = getRegistrar();							
			Game game = null;
			GameType type = GameType.valueOf(gameTypeStr);
			
			switch (type) {
			case PENTE:
				game = new PenteGame();
				break;
			case MANANTIALES:
				game = new ManantialesGame();
				break;				
			}
			
			game.initialize(GameType.valueOf(gameTypeStr));
			game = registrar.persist(game);
			gamePlayer = registrar.registerPlayer(game, player, preferedColor);
		} catch (InvalidRegistrationException e) {
			e.printStackTrace();
			throw new GameException(e);
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (NoClassDefFoundError e) {
			e.printStackTrace();
		}
		return gamePlayer;
	}
	
	public GamePlayer joinPendingGame (Game game, Player player, 
			Color preferredColor) 
	{
		GamePlayer ret = null;
		RegistrarRemote registrar = getRegistrar();
		try {
			ret = registrar.registerPlayer(game, player, preferredColor);
		} catch (InvalidRegistrationException e) {
			e.printStackTrace();
			throw new GameException (e);
		}
		
		return ret;
	}
	
	public boolean quitGame (GamePlayer player) {
		boolean ret = false;
		try {
			RegistrarRemote registrar = getRegistrar();
			registrar.unregisterPlayer(player);
			ret = true;
		} catch (InvalidRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/*
	 * Starts a new game with a given player and the number of selected AI robots. 
	 */
	public GamePlayer startNewGameWithAI (Player player, Color preferredColor, 
			String gameTypeStr, String [] strategies)
	{
		GamePlayer ret = null;
		try {
			RegistrarRemote registrar = getRegistrar();
			GameType gameType = GameType.valueOf(gameTypeStr);
			if (gameType.equals(GameType.PENTE)) {
				Game game = new PenteGame ();
				game.initialize(gameType);
				registrar.persist(game);
				ret = registrar.registerPlayer(game, player, preferredColor);
				for (int i = 0; i < strategies.length; i++) {
					if (strategies [ i ].equals("HUMAN"))
						continue;
					PenteStrategy strategy = PenteStrategy.valueOf(strategies [ i ]);
					Player robot = new Player (strategy.name() + "-" + (i + 1));
					PenteStrategyPlayer agent = new PenteStrategyPlayer (game, robot, Color.UNKNOWN, strategy);
					registrar.registerAgent(game, robot, agent);
				}
			} else 
				ret = startNewGame(player, preferredColor, gameTypeStr);
			
		} catch (InvalidRegistrationException e) {
			e.printStackTrace();
			throw new GameException (e);
	 	}
		
		return ret;
	}
	
	public Player login(String name){
		RegistrarRemote registrar = getRegistrar();
		return registrar.login(name);
	}
	
	
	public List<Game> getUnfinishedGames(Player player){
		RegistrarRemote registrar = getRegistrar();
		return registrar.getUnfinishedGames(player);
	}
	
	public List<Game> getPendingGames(Player player){
		RegistrarRemote registrar = getRegistrar();
		return registrar.getPendingGames(player);
	}

	public Game getGame(int gameId) {

		SharedBoardRemote sharedBoard = getSharedBoard();
		return sharedBoard.getGame(gameId);
	}

	public GameGrid getGameGrid(int gameId) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		return sharedBoard.getGameGrid(gameId);
	}

	public List<GamePlayer> getPlayers(int gameId) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		List<GamePlayer> players = sharedBoard.getPlayers(gameId);
		ArrayList<GamePlayer> ret = new ArrayList<GamePlayer>();
		for (GamePlayer player : players) {
			ret.add (player);
		}
		
		return ret;
	}

	public void doMove(Move move) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		try {
			sharedBoard.move(move);
		} catch (InvalidMoveException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
	}

	public List<Move> getMoves(int gameId) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		List<Move> moves = sharedBoard.getMoves(gameId);
		return moves;
	}

	public Move updateMove(Move move) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		return sharedBoard.updateMove(move);
	}
}
