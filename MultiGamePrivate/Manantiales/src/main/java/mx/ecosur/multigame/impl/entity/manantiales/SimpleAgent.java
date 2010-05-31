//copyright

package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridMove;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.Move;
import mx.ecosur.multigame.model.implementation.AgentImpl;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;

import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author awaterma@ecosur.mx
 */
@Entity
public class SimpleAgent extends ManantialesPlayer implements AgentImpl {

	private static final long serialVersionUID = 8878695200931762776L;

	@EJB
    private SharedBoardLocal sharedBoard;

    private AgentType type;

    private ManantialesMove lastMove;

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

    @OneToOne
    public ManantialesMove getLastMove() {
        return lastMove;
    }

    public void setLastMove(ManantialesMove lastMove) {
        this.lastMove = lastMove;
    }    

    public AgentType getType() {
        return type;
    }

    public void setType(AgentType type) {
        this.type = type;
    }

    public void processEvent(Message message) {
        logger.info("DEBUG -- SimpleAgent [" + this.toString() + "] processing message: " + message);
        try {
            GameEvent gameEvent = GameEvent.valueOf(message.getStringProperty(
                "GAME_EVENT"));
            ObjectMessage msg = (ObjectMessage) message;

            if (gameEvent.equals(GameEvent.GAME_CHANGE)) {
                /* All unacknowledged suggestions addressed to this player are acknowledged */
                ManantialesGame game = (ManantialesGame) msg.getObject();
                if (game.getMode().equals(Mode.BASIC_PUZZLE) || game.getMode().equals(Mode.SILVO_PUZZLE)) {
                    Set<PuzzleSuggestion> suggestions = game.getSuggestions();
                    for (PuzzleSuggestion suggestion : suggestions) {
                        if (suggestion.getStatus().equals(SuggestionStatus.EVALUATED)) {
                            if (suggestion.getMove().getPlayer().equals(this)) {
                                suggestion.setStatus(SuggestionStatus.ACCEPT);
                                sharedBoard.doMove(new Game(game), new Move(suggestion.getMove()));
                            }
                        }
                    }
                }
            }

        } catch (JMSException e) {
            logger.warning("Not able to process game message: " + e.getMessage());
            e.printStackTrace();
        } catch (InvalidMoveException e) {
            logger.warning("Not able to process move: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean ready() {
        return isTurn();
    }

    /* Simply returns a simple move response.  No suggestions are made by the Agent */
    public MoveImpl determineNextMove(GameImpl impl) {
        lastMove = new ManantialesMove();
        lastMove.setPlayer(this);

        ManantialesGame game = (ManantialesGame) impl;
        lastMove.setDestinationCell (generateMove(game));

        /* Suggest a brand new move */
        if (lastMove.getDestinationCell() == null) {
            lastMove = upgradeMove(game, lastMove);
        }

        /* Pair modality */
        lastMove.setMode (game.getMode());

        /* Debug */
        logger.info ("DEBUG -- Agent [" + this.toString() + "] suggests: " + lastMove.toString());
        
        return lastMove;
    }

    private ManantialesMove upgradeMove (ManantialesGame game, ManantialesMove lastMove) {
        Set<GridCell> filter = new HashSet<GridCell>();
        for (GridCell cell : game.getGrid().getCells()) {
            if ( cell.getColor().equals(this.getColor()) ) {
                filter.add (cell);
            }

        }

                /* If the player has cells on its grid, attempt to upgrade those cells */
        if (filter.size() > 0) {
            for (GridCell cell : filter) {
                Ficha ficha = (Ficha) cell;
                if (ficha.getType().equals(TokenType.UNDEVELOPED)) {
                    Ficha destination = new Ficha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                    if (this.getForested() < 6) {
                        destination.setType (TokenType.MANAGED_FOREST);
                        lastMove.setDestinationCell(ficha);
                        break;
                    } else if (this.getModerate() < 6) {
                        destination.setType (TokenType.MODERATE_PASTURE);
                        lastMove.setDestinationCell(ficha);
                        break;
                    }
                }
            }

            /* Search for Moderate Pastures to upgrade */
            if (lastMove.getDestinationCell() == null) {
                for (GridCell cell : filter) {
                    Ficha ficha = (Ficha) cell;
                    /* Convert Moderate to Intensive */
                    if (ficha.getType().equals(TokenType.MODERATE_PASTURE)) {
                        Ficha destination = new Ficha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                        destination.setType(TokenType.INTENSIVE_PASTURE);
                        lastMove.setCurrentCell (ficha);
                        lastMove.setDestinationCell(destination);
                        break;
                    } else if (ficha.getType().equals(TokenType.MANAGED_FOREST)) {
                        Ficha destination = new Ficha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                        destination.setType(TokenType.MODERATE_PASTURE);
                        lastMove.setCurrentCell (ficha);
                        lastMove.setDestinationCell(destination);
                        break;
                    }
                }
            }
        }

        return lastMove;
    }

    private Ficha generateMove (ManantialesGame game) {
        Ficha ret = null;

        List<Ficha> candidates = generateCandidates(game);
        for (Ficha candidate : candidates) {
            switch (candidate.getType()) {
                case MANAGED_FOREST:
                    if (this.getForested() < 6) {
                        ret = candidate;
                        break;
                    }
                case MODERATE_PASTURE:
                    if (this.getModerate() < 6) {
                        ret = candidate;
                        break;
                    }
                case INTENSIVE_PASTURE:
                    if (this.getIntensive() < 6) {
                        ret = candidate;
                        break;
                    }
                    break;
                case VIVERO:
                    if (this.getVivero() < 6) {
                        ret = candidate;
                        break;
                    }
                    break;
                case SILVOPASTORAL:
                    if (this.getSilvo() < 6) {
                        ret = candidate;
                        break;
                    }
                    break;
                default:
                    break;
            }

            if (ret != null)
                break;
        }

        return ret;
    }

    private List<Ficha> generateCandidates(ManantialesGame game) {
        List<Ficha> ret = new ArrayList<Ficha>();
        GameGrid grid = game.getGrid();

        int startrow, startcol, endrow, endcol;
        switch (getColor()) {
            case YELLOW:
                startrow = 1;
                startcol = 1;
                endrow = 5;
                endcol = 5;
                break;
            case PURPLE:
                startrow = 4;
                startcol = 1;
                endrow = game.getRows() + 1;
                endcol = 5;
                break;
            case RED:
                startrow = 1;
                startcol = 4;
                endrow = 5;
                endcol = game.getColumns() + 1;
                break;
            case BLACK:
                startrow = 4;
                startcol = 4;
                endrow = game.getRows() + 1;
                endcol = game.getColumns() + 1;
                break;
            default:
                throw new RuntimeException ("Unknown Color!!");
        }

        for (int row = startrow; row < endrow; row++) {
            for (int col = startcol; col < endcol; col++) {
                Ficha ficha = new Ficha (col, row, getColor(), TokenType.MANAGED_FOREST);
                if (isGoodLocation (ficha)  && grid.getLocation(ficha) == null)
                    ret.add(ficha);
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

        /* Check for Manantial */
        ret = (column !=4 && row != 4);
        return ret;
    }

}
