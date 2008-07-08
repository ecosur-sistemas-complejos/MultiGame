package mx.ecosur.multigame.ejb.jms;

import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import mx.ecosur.multigame.GameEvent;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.RegistrarLocal;
import mx.ecosur.multigame.ejb.SharedBoardLocal;
import mx.ecosur.multigame.ejb.entity.Player;

@MessageDriven(mappedName = "CHECKERS")
public class CheckersChat implements MessageListener {

	@EJB
	private SharedBoardLocal sharedBoard;

	@EJB
	private RegistrarLocal registrar;

	/**
	 * Simple onMessage method appends the name of the Player that sent a
	 * message, and the messages contents to the SharedBoard stream
	 */
	public void onMessage(Message message) {

		MapMessage map = (MapMessage) message;
		try {
			// TODO: Add selector or filter to only treat CHAT messages
			GameEvent gameEvent = GameEvent.valueOf(map
					.getStringProperty("GAME_EVENT"));
			if (gameEvent.equals(GameEvent.CHAT)) {
				
				//get data from message
				int gameId = map.getIntProperty("GAME_ID");
				GameType gameType = GameType.valueOf(map.getString("GAME_TYPE"));

				String senderName = map.getString("senderName");
				double dateSent = map.getDouble("dateSent");
				Date date = new Date(new Double(dateSent).longValue());
				String body = map.getString("body");
				
				//locate shared board and player
				sharedBoard.locateSharedBoard(gameType, gameId);
				Player player = registrar.locatePlayer(senderName);

				// TODO: Add relation between move and chatMessage
				sharedBoard.addMessage(player, body, date);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
