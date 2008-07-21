package mx.ecosur.multigame.flexClient.service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.flexClient.exception.GameException;
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

	public GamePlayer registerPlayer(Player player, Color preferedColor,
			String gameTypeStr) throws InvalidRegistrationException {
		RegistrarRemote registrar = getRegistrar();
		GameType gameType = GameType.valueOf(gameTypeStr);
		GamePlayer gamePlayer;
		try {
			gamePlayer = registrar.registerPlayer(player, preferedColor,
					gameType);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new GameException(e);
		} catch (InvalidRegistrationException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
		return gamePlayer;
	}

	public Game getGame(String gameTypeStr) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		GameType gameType = GameType.valueOf(gameTypeStr);
		try {
			sharedBoard.locateSharedBoard(gameType);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
		return sharedBoard.getGame();
	}

	public GameGrid getGameGrid() {
		SharedBoardRemote sharedBoard = getSharedBoard();
		FlexSession session = FlexContext.getFlexSession();
		return sharedBoard.getGameGrid();
	}

	public List<GamePlayer> getPlayers() {
		SharedBoardRemote sharedBoard = getSharedBoard();
		List<GamePlayer> players = sharedBoard.getPlayers();
		return players;
	}

	public Map<String, Boolean> getValidMoves(GamePlayer gamePlayer, Cell cell) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		HashMap<String, Boolean> validMoves = new HashMap<String, Boolean>();
		Move move;
		Cell destination;
		int rows = sharedBoard.getGame().getRows();
		int cols = sharedBoard.getGame().getColumns();
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
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
	}
	
	public List<Move> getMoves(){
		SharedBoardRemote sharedBoard = getSharedBoard();
		List<Move> moves = sharedBoard.getMoves();
		return moves;
	}
	
	public Move updateMove(Move move){
		SharedBoardRemote sharedBoard = getSharedBoard();
		return sharedBoard.updateMove(move);
	}

	// TODO: This method is temporary for development purposes. It will be
	// deleted
	public void unregisterAllPlayers(String gameTypeStr)
			throws InvalidRegistrationException {
		RegistrarRemote registrar = getRegistrar();
		SharedBoardRemote sharedBoard = getSharedBoard();
		try {
			if (sharedBoard.getGame() != null) {
				for (GamePlayer gp : sharedBoard.getGame().getPlayers()) {
					registrar.unregisterPlayer(gp);
					logger.info("Unregistering player " + gp.getPlayer().getId()
							+ " from game " + sharedBoard.getGame().getId());
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
	}
}
