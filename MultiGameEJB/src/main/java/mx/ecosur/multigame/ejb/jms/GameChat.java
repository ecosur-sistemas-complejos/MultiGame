package mx.ecosur.multigame.ejb.jms;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import mx.ecosur.multigame.GameEvent;
import mx.ecosur.multigame.ejb.RegistrarLocal;
import mx.ecosur.multigame.ejb.SharedBoardLocal;
import mx.ecosur.multigame.ejb.entity.ChatMessage;

@MessageDriven(mappedName = "CHECKERS")
public class GameChat implements MessageListener {

	private static Logger logger = Logger.getLogger(GameChat.class
			.getCanonicalName());
	@EJB
	private SharedBoardLocal sharedBoard;

	@EJB
	private RegistrarLocal registrar;

	/**
	 * Simple onMessage method appends the name of the Player that sent a
	 * message, and the messages contents to the SharedBoard stream
	 */
	public void onMessage(Message message) {

		ObjectMessage msg = (ObjectMessage) message;
		GameEvent gameEvent;
		int gameId;
		try {
			// TODO: Add selector or filter to only treat CHAT messages
			gameEvent = GameEvent.valueOf(msg.getStringProperty("GAME_EVENT"));
			gameId = msg.getIntProperty("GAME_ID");
			if (gameEvent.equals(GameEvent.CHAT)) {
				ChatMessage chatMessage = (ChatMessage) msg.getObject();
				sharedBoard.addMessage(chatMessage);
			}
		} catch (JMSException e) {
			logger.warning("Not able to save game message: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
