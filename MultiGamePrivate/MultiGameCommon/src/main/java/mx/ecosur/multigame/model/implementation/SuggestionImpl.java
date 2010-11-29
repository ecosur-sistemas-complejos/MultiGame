//copyright

package mx.ecosur.multigame.model.implementation;

import mx.ecosur.multigame.enums.SuggestionStatus;

/**
 * @author awaterma@ecosur.mx
 */
public interface SuggestionImpl extends Implementation {

    public int getId();

    public MoveImpl listMove();

    /* Allows for the attaching of a detached move entity */
    public void attachMove (MoveImpl move);

    public GamePlayerImpl listSuggestor();

    /* Allows for the re-attaching of a detached GamePlayer entity */
    public void attachSuggestor (GamePlayerImpl suggestor);        

    public SuggestionStatus getStatus();

    public void setStatus(SuggestionStatus status);
}
