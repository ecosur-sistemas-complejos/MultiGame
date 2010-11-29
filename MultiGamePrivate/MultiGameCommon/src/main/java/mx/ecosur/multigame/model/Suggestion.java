//copyright

package mx.ecosur.multigame.model;

import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.model.implementation.GamePlayerImpl;
import mx.ecosur.multigame.model.implementation.Implementation;
import mx.ecosur.multigame.model.implementation.MoveImpl;
import mx.ecosur.multigame.model.implementation.SuggestionImpl;

/**
 * @author awaterma@ecosur.mx
 */
public class Suggestion implements Model {

    private SuggestionImpl implementation;

    public Suggestion () {
        super();
    }

    public Suggestion (SuggestionImpl impl) {
        this.implementation = impl;
    }

    public int getId() {
        return implementation.getId();
    }

    public void attachMove (MoveImpl move) {
        implementation.attachMove(move);
    }

    public Move listMove() {
        return new Move (implementation.listMove());
    }

    public void attachSuggestor (GamePlayerImpl player) {
        implementation.attachSuggestor (player);
    }

    public GamePlayer listSuggestor() {
        return new GamePlayer (implementation.listSuggestor());
    }
    
    public SuggestionStatus getStatus() {
        return implementation.getStatus();
    }

    public void setStatus(SuggestionStatus status) {
        implementation.setStatus(status);
    }

    public void setImplementation(Implementation impl) {
        this.implementation = (SuggestionImpl) impl;
    }

    public SuggestionImpl getImplementation() {
        return implementation;
    }
}
