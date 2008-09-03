package mx.ecosur.multigame.ejb.jms;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import mx.ecosur.multigame.GameEvent;
import mx.ecosur.multigame.ejb.SharedBoardLocal;
import mx.ecosur.multigame.ejb.entity.ChatMessage;

@MessageDriven(mappedName = "CHECKERS")
public class GameChat implements MessageListener {

	private static Logger logger = Logger.getLogger(GameChat.class
			.getCanonicalName());
	@EJB
	private SharedBoardLocal sharedBoard;

	/**
	 * Simple onMessage method appends the name of the Player that sent a
	 * message, and the messages contents to the SharedBoard stream
	 */
	public void onMessage(Message message) {

		ObjectMessage msg = (ObjectMessage) message;
		GameEvent gameEvent;
		try {
			gameEvent = GameEvent.valueOf(msg.getStringProperty("GAME_EVENT"));
			// TODO: Instead of this manual filter add selector or filter to only treat CHAT messages
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
