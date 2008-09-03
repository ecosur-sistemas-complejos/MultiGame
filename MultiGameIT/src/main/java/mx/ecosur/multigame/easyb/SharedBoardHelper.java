package mx.ecosur.multigame.easyb;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidMoveException;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;
import mx.ecosur.multigame.ejb.entity.pente.PentePlayer;

import javax.naming.NamingException;

public class SharedBoardHelper {
	
	public RegistrarRemote registrar;
	
	public SharedBoardRemote board;
	
	private HashMap <String, GamePlayer> playerMap;
	
	public SharedBoardHelper () {
		try {
			InitialContext ic = new InitialContext();
			board = (SharedBoardRemote) ic.lookup(
					"mx.ecosur.multigame.ejb.SharedBoardRemote");
			registrar = (RegistrarRemote) ic.lookup(
					"mx.ecosur.multigame.ejb.RegistrarRemote");
			playerMap = new HashMap<String, GamePlayer>();
		} catch (NamingException e) {
			e.printStackTrace();
			throw new RuntimeException (e);
		} 
	}
	
	public int[] getCenter (int gameId) throws RemoteException {
		Game game = board.getGame(gameId);
		Dimension dim = game.getSize();
		int rows = (int) dim.getHeight()/2;
		int columns = (int) dim.getWidth()/2;
		int [] ret = { rows, columns };
		return ret;
	}
	
	public void registerPlayer (String playerName, String color, 
			String gameType) 
	{
		try {
			GameType game = GameType.valueOf(gameType);
			Color favoriteColor = Color.valueOf(color);
			Player registrant = registrar.locatePlayer(playerName);
			GamePlayer player = registrar.registerPlayer(registrant, favoriteColor, game);
			playerMap.put(playerName, player);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void unRegisterPlayer (String type, String playerName) 
	{
		try {
			Game game = registrar.locateGame(playerMap.get(playerName).getPlayer(), GameType.valueOf(type));
			List<GamePlayer> players = game.getPlayers();
			GamePlayer player = playerMap.remove (playerName);
			registrar.unregisterPlayer(player);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void makePenteMove (int row, int column, String playerName) throws 
		RemoteException, InvalidMoveException 
	{
		GamePlayer player = playerMap.get(playerName);
		Cell cell = new Cell (row, column, player.getColor());
		PenteMove move = new PenteMove ((PentePlayer) player, cell);
		Move validated = board.validateMove(move);
		board.move(validated);
	}
}
