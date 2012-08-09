//copyright

package mx.ecosur.multigame.manantiales.entity;

import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.grid.entity.GridPlayer;
import mx.ecosur.multigame.manantiales.enums.AgentType;
import mx.ecosur.multigame.manantiales.enums.Mode;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.manantiales.util.MovesValidator;
import mx.ecosur.multigame.model.interfaces.Game;
import mx.ecosur.multigame.model.interfaces.Move;
import mx.ecosur.multigame.model.interfaces.Suggestion;
import org.apache.commons.lang.builder.HashCodeBuilder;

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

    private static Random R = new Random();

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
        List<Move> ret = new ArrayList<Move>();
        ManantialesGame game = (ManantialesGame) impl;
        /* Calculate new positions */
        Set<Move> newMoves = findNewMoves(game);
        /* Calculate upgrades */
        Set<Move> upgradeMoves = findUpgradeMoves(game);


        /* Add in all generated moves */
        ret.addAll(newMoves);
        ret.addAll(upgradeMoves);

        /* Visit an validate suggested moves */
        MovesValidator v = new MovesValidator(game, ret);
        v.visit();
        /* Get the list of invalid moves and strike them from the return list */
        ret = v.getMoves();

        /* Add in swap moves (failsafe if MoveValidator fails). Give at least three tries. */
        if (ret.equals(Collections.EMPTY_LIST) || ret.size() < 3)
            ret.addAll(findSwapMoves(game));

        /* Randomize moves */
        Collections.shuffle(ret);
        return ret;
    }

    /* For testing suggestions in single-player */
    public Suggestion offerSuggestion(Game g) {
        PuzzleSuggestion ret = null;
        ManantialesGame game = (ManantialesGame) g;

        if (game.getMode().equals(Mode.BASIC_PUZZLE) || game.getMode().equals(Mode.SILVO_PUZZLE) &&
                game.getSuggestions() == Collections.EMPTY_SET || game.getSuggestions().size() == 0)
        {

            ManantialesPlayer p = null;
            for (GridPlayer gp : game.getPlayers()) {
                if (gp.getColor().equals(Color.YELLOW)) {
                    p = (ManantialesPlayer) gp;
                    break;
                }
            }

            List<ManantialesFicha> filter = new LinkedList<ManantialesFicha>();
            for (GridCell c : game.getGrid().getCells()) {
                if (c.getColor().equals(p.getColor()))
                    filter.add((ManantialesFicha) c);
            }

            if (!filter.isEmpty() && filter.size() > 1 && R.nextBoolean()) {
                Collections.shuffle(filter);
                ret = new PuzzleSuggestion();
                ret.setSuggestor(this);
                ret.setStatus(SuggestionStatus.UNEVALUATED);
                /* Suggest a swap move */
                ret.setMove(new ManantialesMove(p, filter.get(0), filter.get(1)));
            }
        }

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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().
        append(getType()).
        append(getId()).
        append(getColor()).
        append(getName()).
        toHashCode();
    }
}
