//copyright

package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.util.manantiales.MovesValidator;
import mx.ecosur.multigame.model.interfaces.Game;
import mx.ecosur.multigame.model.interfaces.Move;
import mx.ecosur.multigame.model.interfaces.Suggestion;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.*;

/**
 * @author awaterma@ecosur.mx
 */
@Entity
public class SimpleAgent extends AbstractAgent {

    private static final long serialVersionUID = 8878695200931762776L;

    private AgentType type;

    public SimpleAgent() {
        super();        
    }

    public SimpleAgent(GridRegistrant player, Color favoriteColor, AgentType type) {
        super (player, favoriteColor);
        this.type = type;
    }

    public void initialize() {
        // do nothing 
    }   

    @Enumerated(EnumType.STRING)
    public AgentType getType() {
        return type;
    }

    public void setType(AgentType type) {
        this.type = type;
    }

    public Suggestion processSuggestion (Game impl, Suggestion suggestionImpl) {

        /* Evaluated Suggestions are automatically acknowledged and accepted */
        ManantialesGame game = (ManantialesGame) impl;
        PuzzleSuggestion suggestion = (PuzzleSuggestion) suggestionImpl;
        if (suggestion.getMove().getPlayer().equals(this)) {
            suggestionImpl.setStatus(SuggestionStatus.ACCEPT);
        }

        return suggestionImpl;
    }

    public boolean ready() {
        return isTurn();
    }

    /* Simply returns a simple move response.  No suggestions are made by the Agent */
    public List<Move> determineMoves(Game impl) {
        List<Move> ret = Collections.EMPTY_LIST;      // Set to an empty list as there is an intention to sometimes return no moves (puzzle games)
        Random r = new Random();
        ManantialesGame game = (ManantialesGame) impl;
        ret = new ArrayList<Move>();
        ret.addAll(findNewMoves(game));
        ret.addAll(findUpgradeMoves(game));
        ret.addAll(findSwapMoves(game));
        MovesValidator v = new MovesValidator(game, ret);
        v.visit();
        ret = v.getValidMoves();
        Collections.shuffle(ret);
        return ret;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        SimpleAgent ret = new SimpleAgent();
        ret.setId(getId());
        ret.setTurn(isTurn());
        ret.setType(getType());
        ret.setColor(getColor());
        ret.setName(getName());
        ret.setScore(getScore());
        ret.setForested(getForested());
        ret.setIntensive(getIntensive());
        ret.setModerate(getModerate());
        ret.setSilvo(getSilvo());
        ret.setVivero(getVivero());
        return ret;                
    }


}
