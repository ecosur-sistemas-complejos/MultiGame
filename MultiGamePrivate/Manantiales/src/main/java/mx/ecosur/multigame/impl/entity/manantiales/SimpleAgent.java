//copyright

package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.implementation.AgentImpl;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;
import mx.ecosur.multigame.model.implementation.SuggestionImpl;

import javax.persistence.Entity;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author awaterma@ecosur.mx
 */
@Entity
public class SimpleAgent extends ManantialesPlayer implements AgentImpl {

	private static final long serialVersionUID = 8878695200931762776L;

    private AgentType type;

    private static final Logger logger = Logger.getLogger(SimpleAgent.class.getCanonicalName());

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

    public AgentType getType() {
        return type;
    }

    public void setType(AgentType type) {
        this.type = type;
    }

    public SuggestionImpl processSuggestion (GameImpl impl, SuggestionImpl suggestionImpl) {

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
    public Set<MoveImpl> determineMoves(GameImpl impl) {
        ManantialesGame game = (ManantialesGame) impl;
        Set<MoveImpl> ret = new LinkedHashSet<MoveImpl>();
        List<Ficha> fichas = generateCandidates(game);
        for (Ficha ficha : fichas) {
            if (this.isGoodLocation(ficha)) {
                ManantialesMove move = new ManantialesMove();
                move.setPlayer(this);
                move.setDestinationCell(ficha);
                move.setMode (game.getMode());
                ret.add(move);
            }
        }

        ret.addAll(findUpgradeMoves((ManantialesGame) impl));
        return ret;
    }

    private Set<MoveImpl> findUpgradeMoves (ManantialesGame game) {
        Set<MoveImpl> ret = new LinkedHashSet<MoveImpl>();

        Set<GridCell> filter = new HashSet<GridCell>();
        for (GridCell cell : game.getGrid().getCells()) {
            if ( cell.getColor().equals(this.getColor()) ) {
                filter.add (cell);
            }
        }

        for (GridCell cell : filter) {
            ManantialesMove move = new ManantialesMove();
            move.setPlayer(this);
            move.setMode(game.getMode());

            Ficha ficha = (Ficha) cell;
            /* Convert Moderate to Intensive */
            if (ficha.getType().equals(TokenType.MODERATE_PASTURE)) {
                Ficha destination = new Ficha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                destination.setType(TokenType.INTENSIVE_PASTURE);
                move.setCurrentCell (ficha);
                move.setDestinationCell(destination);
                break;
            } else if (ficha.getType().equals(TokenType.MANAGED_FOREST)) {
                Ficha destination = new Ficha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                destination.setType(TokenType.MODERATE_PASTURE);
                move.setCurrentCell (ficha);
                move.setDestinationCell(destination);
            }

            if (move.getDestinationCell() != null)
                ret.add(move);
        }

        return ret;
    }

    private List<Ficha> generateCandidates(ManantialesGame game) {
        List<Ficha> ret = new ArrayList<Ficha>();
        GameGrid grid = game.getGrid();

        int startrow, startcol, endrow, endcol;
        switch (getColor()) {
            case YELLOW:
                startrow = 0;
                startcol = 0;
                endrow = 5;
                endcol = 5;
                break;
            case PURPLE:
                startrow = 4;
                startcol = 0;
                endrow = game.getRows();
                endcol = 5;
                break;
            case RED:
                startrow = 0;
                startcol = 4;
                endrow = 5;
                endcol = game.getColumns();
                break;
            case BLACK:
                startrow = 4;
                startcol = 4;
                endrow = game.getRows();
                endcol = game.getColumns();
                break;
            default:
                throw new RuntimeException ("Unknown Color!!");
        }

        for (int row = startrow; row < endrow; row++) {
            for (int col = startcol; col < endcol; col++) {
                Ficha ficha = new Ficha (col, row, getColor(), TokenType.UNKNOWN);
                if (isGoodLocation (ficha)  && grid.getLocation(ficha) == null) {
                    if (this.getForested() < 6)
                        ficha.setType(TokenType.MANAGED_FOREST);
                    else if (this.getModerate() < 6)
                        ficha.setType(TokenType.MODERATE_PASTURE);
                    else if (this.getIntensive () < 6)
                        ficha.setType(TokenType.INTENSIVE_PASTURE);
                    else if (game.getMode().equals(Mode.SILVOPASTORAL)) {
                        if (this.getVivero() < 6) {
                            ficha.setType(TokenType.VIVERO);
                        }
                    } else if (game.getMode().equals(Mode.SILVO_PUZZLE)) {
                        if (this.getSilvo() < 6)
                            ficha.setType(TokenType.SILVOPASTORAL);
                    }

                    ret.add(ficha);
                }
            }
        }

        return ret;
    }

    private boolean isGoodLocation (Ficha ficha) {
        boolean ret;
        Color color = ficha.getColor();
        int column = ficha.getColumn(), row = ficha.getRow();

        switch (color) {
            case YELLOW:
                ret = (column < 5 && row < 5);
                break;
            case PURPLE:
                ret = (column < 5 && row > 3);
                break;
            case RED:
                ret = (column > 3 && row < 5);
                break;
            case BLACK:
                ret = (column > 3 && row > 3);
                break;
            default:
                ret = false;
        }

        if (column % 2 == 0)
            ret = ret && row % 2 == 0;
        else
            ret = ret && row % 2 != 0;

        /* Check for Manantial */
        ret = ret && (column !=4 && row != 4);
        return ret;
    }

}
