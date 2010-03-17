package mx.ecosur.multigame.impl.util.manantiales;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.impl.entity.manantiales.PuzzleSuggestion;
import mx.ecosur.multigame.impl.enums.manantiales.ManantialesEvent;
import mx.ecosur.multigame.model.Game;

import javax.jms.*;
import java.io.Serializable;

/**
 * @author awaterma@ecosur.mx
 */
public class ManantialesMessageSender extends MessageSender {


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
