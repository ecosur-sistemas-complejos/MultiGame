/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

package mx.ecosur.multigame.flexClient.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.flexClient.exception.GameException;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridGame;
import mx.ecosur.multigame.impl.model.GridMove;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.enums.gente.*;

import mx.ecosur.multigame.model.Agent;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Move;
import mx.ecosur.multigame.model.Registrant;


import flex.messaging.FlexContext;
import flex.messaging.FlexSession;

public class GameService {

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
	
	public GridPlayer startNewGame(GridRegistrant player, Color preferedColor,
			String gameTypeStr) {
		GridPlayer gamePlayer = null;
		try {
			RegistrarRemote registrar = getRegistrar();							
			GridGame game = null;
			GameType type = GameType.valueOf(gameTypeStr);
			
			switch (type) {
			case PENTE:
				game = new GenteGame();
				break;
			case MANANTIALES:
				game = new ManantialesGame();
				break;				
			}
			
			GamePlayer playerModel = registrar.registerPlayer(new Game(game), 
					new Registrant(player));
			gamePlayer = (GridPlayer) playerModel.getImplementation();
			
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
	
	public GridPlayer joinPendingGame (GridGame game, GridRegistrant player, 
			Color preferredColor) 
	{
		GridPlayer ret = null;
		RegistrarRemote registrar = getRegistrar();
		try {
			GamePlayer playerModel = registrar.registerPlayer (new Game(game), 
					new Registrant(player));
			ret = (GridPlayer) playerModel.getImplementation();
			
		} catch (InvalidRegistrationException e) {
			e.printStackTrace();
			throw new GameException (e);
		}
		
		return ret;
	}
	
	public boolean quitGame (GridPlayer player) {
		boolean ret = false;
		try {
			RegistrarRemote registrar = getRegistrar();
			GridGame game = player.getGame();
			registrar.unregister (new Game (game), new GamePlayer (player));
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
	public GridPlayer startNewGameWithAI (GridRegistrant player, Color preferredColor, 
			String gameTypeStr, String [] strategies)
	{
		GridPlayer ret = null;
		try {
			RegistrarRemote registrar = getRegistrar();
			GameType gameType = GameType.valueOf(gameTypeStr);
			if (gameType.equals(GameType.PENTE)) {
				GridGame game = new GenteGame ();	
				GamePlayer gamePlayer = registrar.registerPlayer(new Game(game), 
						new Registrant (player));
				game = (GridGame) gamePlayer.getGame().getImplementation();
				for (int i = 0; i < strategies.length; i++) {
					if (strategies [ i ].equals("HUMAN"))
						continue;
					GenteStrategy strategy = GenteStrategy.valueOf(
							strategies [ i ]);
					GridRegistrant robot = new GridRegistrant (
							strategy.name() + "-" + (i + 1));
					GenteStrategyAgent agent = new GenteStrategyAgent (game, 
							robot, Color.UNKNOWN, strategy);
					registrar.registerAgent (
							new Game(game), new Agent (agent));
				}
				
				ret = (GridPlayer) gamePlayer.getImplementation();
				
			} else 
				ret = startNewGame(player, preferredColor, gameTypeStr);
			
		} catch (InvalidRegistrationException e) {
			e.printStackTrace();
			throw new GameException (e);
	 	}
		
		return ret;
	}
	
	
	public List<GridGame> getUnfinishedGames(GridRegistrant player){
		RegistrarRemote registrar = getRegistrar();
		Collection<Game> games = registrar.getUnfinishedGames(
				new Registrant (player));
		List<GridGame> ret = new ArrayList<GridGame>();
		for (Game game : games) {
			ret.add((GridGame) game.getImplementation());
		}
		return ret;
	}
	
	public List<GridGame> getPendingGames(GridRegistrant player){
		RegistrarRemote registrar = getRegistrar();
		Collection<Game> games = registrar.getPendingGames(new Registrant (
				player));
		List<GridGame> ret = new ArrayList<GridGame>();
		for (Game game : games) {
			ret.add((GridGame) game.getImplementation());
		}
		return ret;
	}

	public GridGame getGame(int gameId) {

		SharedBoardRemote sharedBoard = getSharedBoard();
		Game game = sharedBoard.getGame(gameId);
		return (GridGame) game.getImplementation();
	}

	public GameGrid getGameGrid(int gameId) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		Game game = sharedBoard.getGame(gameId);
		GridGame gridGame = (GridGame) game.getImplementation();
		return gridGame.getGrid();
	}

	public List<GridPlayer> getPlayers(int gameId) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		Game game = sharedBoard.getGame(gameId);
		GridGame gridGame = (GridGame) game.getImplementation();
		return gridGame.getPlayers();
	}

	public void doMove(GridMove move) {
System.out.println("****************");
System.out.println("Move: " + move);
		SharedBoardRemote sharedBoard = getSharedBoard();		
		try {
			GridGame game = move.getPlayer().getGame();
			sharedBoard.doMove(new Game (game), new Move(move));
		} catch (InvalidMoveException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
	}

	public List<GridMove> getMoves(int gameId) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		List<GridMove> moves = new ArrayList<GridMove>();		
		Collection<Move> boardMoves = sharedBoard.getMoves(gameId);
		for (Move move : boardMoves) {
			moves.add((GridMove) move.getImplementation());
		}		
		return moves;
	}

	public GridMove updateMove(GridMove move) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		Move moveModel = sharedBoard.updateMove(new Move(move));
		return (GridMove) moveModel.getImplementation();
	}
	
	public GridRegistrant login (String name) {
		GridRegistrant gr = new GridRegistrant (name);
		RegistrarRemote registrar = this.getRegistrar();
		Registrant registrant = registrar.register(new Registrant (gr));
		return (GridRegistrant) registrant.getImplementation();
	}
}
