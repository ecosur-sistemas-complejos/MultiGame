package mx.ecosur.multigame.impl.util.manantiales;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.impl.entity.manantiales.Suggestion;
import mx.ecosur.multigame.model.Game;

/**
 * @author awaterma@ecosur.mx
 */
public class ManantialesMessageSender extends MessageSender {

    public void sendSuggestionEvaluated (Game game, Suggestion suggestion) {
        System.out.println ("\n****** SENDING EVALUATED SUGGESTION OVER WIRE: " + suggestion + "*****\n");

        super.sendMessage(game.getId(), GameEvent.GAME_CHANGE, suggestion);
    }
}
