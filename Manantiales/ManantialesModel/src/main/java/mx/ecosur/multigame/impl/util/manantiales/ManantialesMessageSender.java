package mx.ecosur.multigame.impl.util.manantiales;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesPlayer;
import mx.ecosur.multigame.impl.entity.manantiales.PuzzleSuggestion;
import mx.ecosur.multigame.model.interfaces.Game;
import mx.ecosur.multigame.model.interfaces.Move;

import java.util.logging.Logger;

/**
 * @author awaterma@ecosur.mx
 */
public class ManantialesMessageSender extends MessageSender {

    private static final Logger logger = Logger.getLogger(ManantialesMessageSender.class.getCanonicalName());

    public void sendSuggestionEvaluated (ManantialesGame game, PuzzleSuggestion suggestion) {
        sendMessage(game.getId(), GameEvent.SUGGESTION_EVALUATED, suggestion);
    }

    public void sendSuggestionApplied (ManantialesGame game, PuzzleSuggestion suggestion) {
        sendMessage(game.getId(), GameEvent.SUGGESTION_APPLIED, suggestion);
    }

    public void sendRoundChange (ManantialesGame game, ManantialesPlayer initiator) {
        sendMessage(game.getId(), GameEvent.GAME_CHANGE, initiator);
    }

    public void sendTimeLimitExpired (ManantialesGame game, ManantialesMove move) {
        sendMessage(game.getId(), GameEvent.EXPIRED, move);
    }
}
