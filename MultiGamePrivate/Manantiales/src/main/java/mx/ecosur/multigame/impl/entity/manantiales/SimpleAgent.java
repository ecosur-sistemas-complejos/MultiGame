//copyright

package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.impl.model.GameGrid;
import mx.ecosur.multigame.impl.model.GridCell;
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
import java.awt.*;
import java.util.HashSet;
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

    public void processEvent(Message message) {
        logger.info("SimpleAgent processing message: " + message);
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
        ManantialesMove ret = new ManantialesMove();
        ret.setPlayer(this);

        ManantialesGame game = (ManantialesGame) impl;
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
                        ret.setDestinationCell(ficha);
                        break;
                    } else if (this.getModerate() < 6) {
                        destination.setType (TokenType.MODERATE_PASTURE);
                        ret.setDestinationCell(ficha);
                        break;
                    }
                }
            }

            /* Search for Moderate Pastures to upgrade */
            if (ret.getDestinationCell() == null) {
                for (GridCell cell : filter) {
                    Ficha ficha = (Ficha) cell;
                    /* Convert Moderate to Intensive */
                    if (ficha.getType().equals(TokenType.MODERATE_PASTURE)) {
                        Ficha destination = new Ficha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                        destination.setType(TokenType.INTENSIVE_PASTURE);
                        ret.setCurrentCell (ficha);
                        ret.setDestinationCell(destination);
                        break;
                    } else if (ficha.getType().equals(TokenType.MANAGED_FOREST)) {
                        Ficha destination = new Ficha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                        destination.setType(TokenType.MODERATE_PASTURE);
                        ret.setCurrentCell (ficha);
                        ret.setDestinationCell(destination);
                        break;
                    }
                }
            }
        }

        /* Suggest a brand new move */
        if (ret.getDestinationCell() == null) {
            GameGrid grid = game.getGrid();
            for (int col = 0; col < game.getColumns() && ret.getDestinationCell () == null; col++) {
                for (int row = 0; row < game.getRows() && ret.getDestinationCell() == null; row++) {
                    if (possibleLocation (col + 1, row + 1)) {
                        GridCell possible = new GridCell (col + 1, row + 1, getColor());
                        GridCell existing = grid.getLocation (possible);
                        if (existing == null) {
                            Ficha dest = null;
                            boolean basic = game.getMode().equals(Mode.CLASSIC) || game.getMode().equals(
                                    Mode.BASIC_PUZZLE);

                            /* Add a new ficha to the move based upon the following order */
                            if (this.getForested() < 6) {
                                dest = new Ficha (col + 1, row + 1, getColor(), TokenType.MANAGED_FOREST);
                            } else if (this.getModerate() < 6) {
                                dest = new Ficha (col + 1, row + 1, getColor(), TokenType.MODERATE_PASTURE);
                            } else if (!basic) {
                                if (this.getVivero() < 6) {
                                    dest = new Ficha (col + 1, row + 1, getColor(), TokenType.VIVERO);
                                }
                            }

                            if (dest != null)
                                ret.setDestinationCell (dest);
                            else
                                throw new RuntimeException ("Unable to construct destination for move!");
                        }
                    }
                }
            }
        }

        logger.info ("Agent suggests: " + ret);

        return ret;
    }

    private boolean possibleLocation (int column, int row) {
        boolean ret;

        switch (getColor()) {
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
                ret = column > 3 && row > 3;
                break;
            default:
                ret = false;
        }

        return ret;
    }

}
