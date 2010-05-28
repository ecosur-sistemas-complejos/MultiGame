package mx.ecosur.multigame.impl.util.manantiales;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.impl.entity.manantiales.PuzzleSuggestion;
import mx.ecosur.multigame.impl.enums.manantiales.ManantialesEvent;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;

import javax.jms.*;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * @author awaterma@ecosur.mx
 */
public class ManantialesMessageSender extends MessageSender {

    private static final Logger logger = Logger.getLogger(ManantialesMessageSender.class.getCanonicalName());    

    @Override
    public void sendMoveComplete(GameImpl game, MoveImpl move) {
        logger.info("DEBUG -- Move Complete for move [" + move.toString());
        super.sendMoveComplete(game, move);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void sendPlayerChange(GameImpl game) {
        logger.info ("DEBUG -- send Player Change for game " + game.toString());
        super.sendPlayerChange(game);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @SuppressWarnings("unchecked")
	public void sendMessage(int gameId, Enum event, Serializable body)
    {
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false,
                            Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(topic);
            ObjectMessage message = session.createObjectMessage();
            message.setIntProperty("GAME_ID", gameId);
            message.setStringProperty("GAME_EVENT", event.toString());
            message.setLongProperty("MESSAGE_ID", getNextMessageId(gameId));
            if (body != null) {
                    message.setObject(body);
            }
            producer.send(message);
            session.close();
            connection.close();
        } catch (JMSException e) {            
            e.printStackTrace();
        }
    }

    public void sendSuggestionEvaluated (Game game, PuzzleSuggestion suggestion) {
        sendMessage(game.getId(), ManantialesEvent.SUGGESTION_EVALUATED, suggestion);
    }

    public void sendSuggestionApplied (Game game, PuzzleSuggestion suggestion) {
        sendMessage(game.getId(), ManantialesEvent.SUGGESTION_APPLIED, suggestion);
    }
    
}
