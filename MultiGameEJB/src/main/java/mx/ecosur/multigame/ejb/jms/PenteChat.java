package mx.ecosur.multigame.ejb.jms;

import java.rmi.RemoteException;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.SharedBoard;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.Player;

@MessageDriven(mappedName = "PENTE")
public class PenteChat implements MessageListener {
	
	@EJB 
	private SharedBoard sharedBoard;

	/**
	 * Simple onMessage method appends the name of the Player that
	 * sent a message, and the messages contents to the SharedBoard
	 * stream
	 */
	public void onMessage(Message message) {
		MapMessage map = (MapMessage) message;
		try {
			Player player = (Player) map.getObject("player");
			String msg = map.getString("message");
			boolean turn = map.getBoolean ("turn");
			sharedBoard.locateSharedBoard(GameType.PENTE);
			if (turn)
				sharedBoard.incrementTurn (player);
			sharedBoard.addMessage(player, msg, null);
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
