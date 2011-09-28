//copyright

package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.GameGrid;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.model.interfaces.Agent;
import mx.ecosur.multigame.model.interfaces.Game;
import mx.ecosur.multigame.model.interfaces.Move;
import mx.ecosur.multigame.model.interfaces.Suggestion;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.*;

/**
 * @author awaterma@ecosur.mx
 */
@Entity
public class SimpleAgent extends ManantialesPlayer implements Agent {

    private static final long serialVersionUID = 8878695200931762776L;

    private static KnowledgeBase kbase;

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
        List<Move> ret = new ArrayList<Move>();
        Random random = null;
        ManantialesGame game = (ManantialesGame) impl;
        boolean requiresRandom = game.getMode().equals(Mode.CLASSIC) || game.getMode().equals(Mode.SILVOPASTORAL);
        if (requiresRandom)
            random = new Random();

        if (!requiresRandom || random.nextInt(6) != 3) {
            List<ManantialesFicha> fichas = generateCandidates(game);
            for (ManantialesFicha ficha : fichas) {
                if (this.isGoodLocation(ficha)) {
                    ManantialesMove move = new ManantialesMove();
                    move.setPlayer(this);
                    move.setDestinationCell(ficha);
                    move.setMode (game.getMode());
                    ret.add(move);
                }
            }
            if (!game.getGrid().isEmpty())                
            ret.addAll(findUpgradeMoves(game));
        }

        if (game.getMode().equals(Mode.CLASSIC) || game.getMode().equals(Mode.SILVOPASTORAL)) {
            ManantialesMove pass = generatePassMove(game);
            ret.add (pass);
        }
        
        Collections.shuffle(ret);
        
        return ret;
    }

    @Transient
    private KnowledgeBase getRuleBase() {
        if (kbase == null)
            kbase = findKBase();
        return kbase;
    }

    protected KnowledgeBase findKBase () {
        KnowledgeBase ret = null;
        ret = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
            "/mx/ecosur/multigame/impl/simple-agent.xml")), ResourceType.CHANGE_SET);
        ret.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return ret;
    }

    public Set<Move> findNewMoves(ManantialesGame game) {
        Set<Move> ret = new LinkedHashSet<Move>();

        List<ManantialesFicha> fichas = generateCandidates(game);
        for (ManantialesFicha ficha : fichas) {
            if (this.isGoodLocation(ficha)) {
                ManantialesMove move = new ManantialesMove();
                move.setPlayer(this);
                move.setDestinationCell(ficha);
                move.setMode (game.getMode());
                ret.add(move);
            }
        }

        return ret;
    }

    public ManantialesMove generatePassMove (ManantialesGame game) {
        ManantialesMove ret = new ManantialesMove ();
        ret.setPlayer(this);
        ret.setBadYear(true);
        ret.setStatus(MoveStatus.UNVERIFIED);
        ret.setMode(game.getMode());
        return ret;
    }

    public Set<Move> findUpgradeMoves (ManantialesGame game) {
        Set<Move> ret = new LinkedHashSet<Move>();

        Set<GridCell> filter = new HashSet<GridCell>();
        for (GridCell cell : game.getGrid().getCells()) {
            if (cell != null && getColor() != null && cell.getColor().equals(this.getColor()) ) {
                filter.add (cell);
            }
        }

        for (GridCell cell : filter) {
            ManantialesMove move = new ManantialesMove();
            move.setPlayer(this);
            move.setMode(game.getMode());

            ManantialesFicha ficha = (ManantialesFicha) cell;
            /* Convert Moderate to Intensive */
            if (ficha.getType().equals(TokenType.MODERATE_PASTURE) && getIntensive() < 6) {
                ManantialesFicha destination = new ManantialesFicha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                destination.setType(TokenType.INTENSIVE_PASTURE);
                move.setCurrentCell (ficha);
                move.setDestinationCell(destination);
            } else if (ficha.getType().equals(TokenType.MANAGED_FOREST) && getModerate() < 6) {
                ManantialesFicha destination = new ManantialesFicha(ficha.getColumn(),ficha.getRow(),ficha.getColor(),ficha.getType());
                destination.setType(TokenType.MODERATE_PASTURE);
                move.setCurrentCell (ficha);
                move.setDestinationCell(destination);
            }
            if (move.getDestinationCell() != null) {
                move.setPlayer(this);
                ret.add(move);
            }
        }

        return ret;
    }

    private List<ManantialesFicha> generateCandidates(ManantialesGame game) {
        List<ManantialesFicha> ret = new ArrayList<ManantialesFicha>();
        GameGrid grid = game.getGrid();

        int startrow, startcol, endrow, endcol;
        switch (getColor()) {
            case YELLOW:
                startrow = 0;
                endrow = 4;
                startcol = 0;
                endcol = 4;
                break;
            case PURPLE:
                startrow = 4;
                endrow = game.getRows();
                startcol = 0;
                endcol = 4;
                break;
            case RED:
                startrow = 0;
                endrow = 4;
                startcol = 4;
                endcol = game.getColumns();
                break;
            case BLACK:
                startrow = 4;
                endrow = game.getRows();
                startcol = 4;
                endcol = game.getColumns();
                break;
            default:
                throw new RuntimeException ("Unknown Color!!");
        }

        for (int row = startrow; row <= endrow; row++) {
            for (int col = startcol; col < endcol; col++) {
                ManantialesFicha ficha = new ManantialesFicha(col, row, getColor(), TokenType.UNKNOWN);
                if (isGoodLocation (ficha)  && grid.getLocation(ficha) == null) {
                    if (this.getForested() < 6)
                        ficha.setType(TokenType.MANAGED_FOREST);
                    else if (this.getModerate() < 6)
                        ficha.setType(TokenType.MODERATE_PASTURE);
                    else if (game.getMode().equals(Mode.SILVOPASTORAL) && getVivero() < 6)
                        ficha.setType(TokenType.VIVERO);
                    ret.add(ficha);
                }
            }
        }

        return ret;
    }

    private boolean isGoodLocation (ManantialesFicha ficha) {
        boolean ret;
        Color color = ficha.getColor();
        int column = ficha.getColumn(), row = ficha.getRow();

        switch (color) {
            case YELLOW:
                ret = (column <= 4 && row <= 4);
                break;
            case PURPLE:
                ret = (column <= 4 && row >= 4);
                break;
            case RED:
                ret = (column >= 4 && row <= 4);
                break;
            case BLACK:
                ret = (column >= 4 && row >= 4);
                break;
            default:
                ret = false;
        }

        if (column == 4 || row == 4) {
            
            /* Check for Manantial */
            if (column == 4 && row == 4)
                ret = false;
        } else {
            if (row % 2 == 0 && column % 2 == 0) {
                // even
                ret = ret && true;

            } else if (row % 2 != 0 && column % 2 != 0) {
                //odd
                ret = ret && true;
            } else
                ret = false;
        }

        if (row > 8 || column > 8)
            ret = false;

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
        ret.kbase = this.kbase;
        return ret;                
    }


}
