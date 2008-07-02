package mx.edu.ecosur.multigame.flexClient.service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
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
				logger.severe("Not able to get new instance of shared board");
				e.printStackTrace();
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
				logger.severe("Not able to get new instance of registrar");
				e.printStackTrace();
			}
		}
		return registrar;
	}

	public Player registerPlayer(Player player, String gameTypeStr)
			throws InvalidRegistrationException {
		RegistrarRemote registrar = getRegistrar();
		GameType gameType = GameType.valueOf(gameTypeStr);
		try {
			player.setColor(registrar.registerPlayer(player, gameType));
			logger.info("player registered with color " + player.getColor());
		} catch (RemoteException e) {
			logger.severe("Not able to register player " + player.getName()
					+ " for gameType " + gameType);
			e.printStackTrace();
		} catch (InvalidRegistrationException e) {
			e.printStackTrace();
			throw (e);
		}
		return player;
	}

	public int getGameId(String gameTypeStr) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		GameType gameType = GameType.valueOf(gameTypeStr);
		try {
			sharedBoard.locateSharedBoard(gameType);
		} catch (RemoteException e) {
			logger.severe("Not able to get game id for gameType " + gameType);
			e.printStackTrace();
		}
		int gameId = sharedBoard.getGame().getId();
		return gameId;
	}

	public GameGrid getGameGrid() {
		SharedBoardRemote sharedBoard = getSharedBoard();
		return sharedBoard.getGameGrid();
	}
	
	public List<Player> getPlayers(){
		SharedBoardRemote sharedBoard = getSharedBoard();
		return sharedBoard.getPlayers();
	}

	public Map<String, Boolean> getValidMoves(Player player, Cell cell) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		HashMap<String, Boolean> validMoves = new HashMap<String, Boolean>();
		Move move;
		Cell destination;
		int rows = sharedBoard.getGame().getRows();
		int cols = sharedBoard.getGame().getColumns();
		String moveCode;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				destination = new Cell(r, c, cell.getColor());
				move = new Move(sharedBoard.getGame(), player, cell, destination);
				try {
					sharedBoard.validateMove(move);
					moveCode = Integer.toString(c) + "#"
					+ Integer.toString(r);
					validMoves.put(moveCode, true);
					logger.info("valid move " + moveCode + "\n" + move);
				} catch (InvalidMoveException e) {
					// Do nothing move is not valid
				} catch (Exception e){
					logger.severe("Exception validating move: " + move);
					//throw(e);
				}
			}
		}
		return validMoves;
	}
	
	public void doMove(Move move){
		SharedBoardRemote sharedBoard = getSharedBoard();
		try {
			move.setGame(sharedBoard.getGame());
			Move validatedMove = sharedBoard.validateMove(move);
			sharedBoard.move(validatedMove);
		} catch (InvalidMoveException e) {
			logger.severe("Exception doing move " + move);
			e.printStackTrace();
		}
	}
	
	//TODO: This method is temporary for development purposes. It will be deleted
	public void unregisterAllPlayers(String gameTypeStr)
			throws InvalidRegistrationException {
		RegistrarRemote registrar = getRegistrar();
		SharedBoardRemote sharedBoard = getSharedBoard();
		GameType gameType = GameType.valueOf(gameTypeStr);
		try {
			if (sharedBoard.getGame() != null) {
				for (Player p : sharedBoard.getGame().getPlayers()) {

					registrar.unregisterPlayer(p, gameType);
					logger.info("Unregistering player " + p.getId()
							+ " from game " + sharedBoard.getGame().getId());
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
