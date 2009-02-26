/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

package mx.ecosur.multigame.flexClient.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Cell;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GameGrid;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.flexClient.exception.GameException;
import mx.ecosur.multigame.pente.PenteStrategy;
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
			GameType gameType = GameType.valueOf(gameTypeStr);
			gamePlayer = registrar.registerPlayer(player, preferedColor, 
					GameType.valueOf(GameType.class, gameTypeStr));
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
				Game game = registrar.createGame(gameType);
				ret = registrar.registerPlayer(game, player, preferredColor);
				for (int i = 0; i < strategies.length; i++) {
					if (strategies [ i ].equals("HUMAN"))
						continue;
					PenteStrategy strategy = PenteStrategy.valueOf(strategies [ i ]);
					Player robot = new Player (strategy.name() + "-" + (i + 1));
					registrar.registerRobot(game, robot, Color.UNKNOWN, strategy);
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
		return players;
	}

	public Map<String, Boolean> getValidMoves(GamePlayer gamePlayer, Cell cell) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		HashMap<String, Boolean> validMoves = new HashMap<String, Boolean>();
		Move move;
		Cell destination;
		int rows = gamePlayer.getGame().getRows();
		int cols = gamePlayer.getGame().getColumns();
		String moveCode;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				destination = new Cell(c, r, cell.getColor());
				move = new Move(gamePlayer, cell, destination);
				try {
					sharedBoard.validateMove(move);
					moveCode = Integer.toString(c) + "#" + Integer.toString(r);
					validMoves.put(moveCode, true);
					logger.info("valid move " + moveCode + "\n" + move);
				} catch (InvalidMoveException e) {
					// do not add move to valid moved but continue executing
				}
			}
		}
		if (validMoves.size() == 0) {
			logger.info("No valid moves for cell " + cell);
		}
		return validMoves;
	}

	public void doMove(Move move) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		try {
			Move validatedMove = sharedBoard.validateMove(move);
			sharedBoard.move(validatedMove);
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
