//copyright

package mx.ecosur.multigame.model.interfaces;

import mx.ecosur.multigame.enums.SuggestionStatus;

import java.io.Serializable;

/**
 * @author awaterma@ecosur.mx
 */
public interface Suggestion extends Serializable {

    public int getId();

    public Move listMove();

    /* Allows for the attaching of a detached move entity */
    public void attachMove (Move move);

    public GamePlayer listSuggestor();

    /* Allows for the re-attaching of a detached GamePlayer entity */
    public void attachSuggestor (GamePlayer suggestor);

    public SuggestionStatus getStatus();

    public void setStatus(SuggestionStatus status);
}
