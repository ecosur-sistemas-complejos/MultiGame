
package mx.ecosur.multigame.flexClient.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
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

	public Player registerPlayer(Player player, String gameTypeStr)
			throws InvalidRegistrationException {
		RegistrarRemote registrar = getRegistrar();
		GameType gameType = GameType.valueOf(gameTypeStr);
		try {
			//TODO: This is a tempory fix to allow a player to reenter a game already started without knowing its id.
			//the client interface should be enhanced to allow a player to select from games that he/she is already in.
			player = registrar.locatePlayer(player.getName());

			player = registrar.registerPlayer(player, gameType);
			
			//TODO: This is a temporary fix until the pente player info is moved to gamePlayer
			if (!player.getClass().equals(Player.class)){
				Player player2 = new Player();
				player2.setColor(player.getColor());
				player2.setGamecount(player.getGamecount());
				player2.setId(player.getId());
				player2.setLastRegistration(player.getLastRegistration());
				player2.setName(player.getName());
				player2.setTurn(player.isTurn());
				player2.setWins(player.getWins());
				player = player2;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new GameException(e);
		} catch (InvalidRegistrationException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
		return player;
	}

	public int getGameId(String gameTypeStr) {
		SharedBoardRemote sharedBoard = getSharedBoard();
		GameType gameType = GameType.valueOf(gameTypeStr);
		try {
			sharedBoard.locateSharedBoard(gameType);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
		int gameId = sharedBoard.getGame().getId();
		return gameId;
	}

	public GameGrid getGameGrid() {
		SharedBoardRemote sharedBoard = getSharedBoard();
		FlexSession session = FlexContext.getFlexSession();
		return sharedBoard.getGameGrid();
	}
	
	public List<Player> getPlayers(){
		SharedBoardRemote sharedBoard = getSharedBoard();
		List<Player> players = sharedBoard.getPlayers();
		
		//TODO: This is a temporary fix until the pente player info is moved to gamePlayer
		List<Player> players2 = new ArrayList<Player>();
		Player player2;
		for (Player player : players){
			if (!player.getClass().equals(Player.class)){
				player2 = new Player();
				player2.setColor(player.getColor());
				player2.setGamecount(player.getGamecount());
				player2.setId(player.getId());
				player2.setLastRegistration(player.getLastRegistration());
				player2.setName(player.getName());
				player2.setTurn(player.isTurn());
				player2.setWins(player.getWins());
				players2.add(player2);
			}else{
				players2.add(player);
			}
		}
		
		return players2;
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
				destination = new Cell(c, r, cell.getColor());
				move = new Move(sharedBoard.getGame(), player, cell, destination);
				try {
					sharedBoard.validateMove(move);
					moveCode = Integer.toString(c) + "#"
					+ Integer.toString(r);
					validMoves.put(moveCode, true);
					logger.info("valid move " + moveCode + "\n" + move);
				} catch (InvalidMoveException e) {
					//do not add move to valid moved but continue executing
				}
			}
		}
		if(validMoves.size() == 0){
			logger.info("No valid moves for cell " + cell);
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
			e.printStackTrace();
			throw new GameException(e);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new GameException(e);
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
			throw new GameException(e);
		}
	}
}
