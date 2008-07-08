package mx.ecosur.multigame.easyb;

import java.rmi.RemoteException;

import javax.naming.InitialContext;

import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import javax.naming.NamingException;

public class SharedBoardHelper {
	
	public SharedBoardRemote board;
	
	public SharedBoardHelper () {
		try {
			InitialContext ic = new InitialContext();
			board = (SharedBoardRemote) ic.lookup("mx.ecosur.multigame.ejb.SharedBoardRemote");
		} catch (NamingException e) {
			throw new RuntimeException (e);
		}
	}
	
	public SharedBoardRemote getBoard (GameType type) throws RemoteException {
		board.locateSharedBoard(type);
		return board;
	}

}
