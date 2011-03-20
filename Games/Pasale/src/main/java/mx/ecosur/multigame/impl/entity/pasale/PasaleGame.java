package mx.ecosur.multigame.impl.entity.pasale;

import mx.ecosur.multigame.enums.GameState;

import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;

import mx.ecosur.multigame.grid.model.*;
import mx.ecosur.multigame.grid.MoveComparator;
import mx.ecosur.multigame.impl.enums.pasale.TokenType;

import mx.ecosur.multigame.model.interfaces.Agent;
import mx.ecosur.multigame.model.interfaces.GamePlayer;
import mx.ecosur.multigame.model.interfaces.Move;
import mx.ecosur.multigame.model.interfaces.Registrant;

import static mx.ecosur.multigame.impl.util.pasale.RuleFunctions.*;
import mx.ecosur.multigame.MessageSender;

import javax.persistence.*;

import org.drools.io.Resource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.io.ResourceFactory;
import org.drools.KnowledgeBase;
import org.drools.audit.WorkingMemoryFileLogger;

import java.util.*;
import java.util.List;
import java.awt.*;

@Entity
public class PasaleGame extends GridGame {

    private static final long serialVersionUID = -8395074059039838349L;

    private static final boolean DEBUG = false;

    private static final int DIMENSIONS = 27;

    private transient KnowledgeBase kbase;

    private transient MessageSender messageSender;

    private StatefulKnowledgeSession session;

    private WorkingMemoryFileLogger logger;

    public PasaleGame() {
        super();
        setRows (DIMENSIONS);
        setColumns(DIMENSIONS);
        setState(GameState.WAITING);
        setCreated(new Date());
        grid = createGrid();
    }


    public PasaleGame(int columns, int rows) {
        this();
        setColumns(columns);
        setRows(rows);
        kbase = null;
    }

    public PasaleGame(int columns, int rows, KnowledgeBase kbase) {
        this(columns, rows);
        this.kbase = kbase;
    }

    @Transient
    public Dimension getDimensions() {
        return new Dimension (getColumns(), getRows());
    }

    private PasaleGrid createGrid() {
        PasaleGrid grid = new PasaleGrid ();
        int river = DIMENSIONS/2;
        if (river % 2 != 1)
                river = river + 1;

        /* Populate the grid */
        for (int col = 0; col <= DIMENSIONS; col++) {
            for (int row = 0; row <= DIMENSIONS; row++) {
                if ( (col + row) % 2 != 0)
                    continue;
                if ( row % 2 == 1 || col % 2 == 1) {
                    /* soil or water */
                    /* TODO:  Dynamically determine the location of rivers on the map */
                    if (row == river || col == river) {
                        PasaleFicha ficha = new PasaleFicha (col, row, mx.ecosur.multigame.grid.Color.UNKNOWN, TokenType.WATER_PARTICLE);
                        grid.updateCell (ficha);
                    } else {
                        PasaleFicha ficha = new PasaleFicha (col, row, mx.ecosur.multigame.grid.Color.UNKNOWN, TokenType.SOIL_PARTICLE);
                        grid.updateCell (ficha);                          
                    }

                } else {
                    PasaleFicha forest = new PasaleFicha (col, row, mx.ecosur.multigame.grid.Color.UNKNOWN, TokenType.FOREST);
                    grid.updateCell (forest);
                }
            }
        }

        return grid;        
    }

    @Transient
    public int getMaxPlayers() {
        return 4;
    }

    public void setMaxPlayers(int maxPlayers) {
        // do nothing
    }

    /* (non-Javadoc)
      * @see GridGame#move(mx.ecosur.multigame.model.interfaces.Move)
      */
    public Move move(Move move) throws InvalidMoveException {
        if (session == null) {
            session = kbase.newStatefulKnowledgeSession();
            session.setGlobal("messageSender", getMessageSender());
            session.setGlobal("dimension", new Integer(getColumns()));
        }

        if (DEBUG) {
            logger = new WorkingMemoryFileLogger(session);
            logger.setFileName("audit");
        }

        /* insert game and move */
        session.insert(this);
        session.insert(move);

        /* start the flow */
        session.startProcess("tablon-flow");
        session.fireAllRules();
        if (logger != null)
            logger.writeToDisk();

        if (moves == null)
            moves = new TreeSet<GridMove>(new MoveComparator());

        moves.add((PasaleMove) move);

        return move;
    }

    public GamePlayer registerPlayer(Registrant registrant) throws InvalidRegistrationException  {
        PasalePlayer player = new PasalePlayer();
        player.setRegistrant((GridRegistrant) registrant);

        for (GridPlayer p : this.getPlayers()) {
            if (p.equals (player))
                throw new InvalidRegistrationException ("Duplicate Registraton!");
        }

        int max = getMaxPlayers();
        if (players.size() == max)
            throw new RuntimeException ("Maximum Players reached!");

        List<mx.ecosur.multigame.grid.Color> colors = getAvailableColors();
        player.setColor(colors.get(0));
        if (player.getColor().equals(mx.ecosur.multigame.grid.Color.YELLOW))
            player.setTurn(true);
        players.add(player);
        
        if (players.size() == max)
            setState(GameState.PLAY);

        return player;
    }

    public Agent registerAgent (Agent agent) throws InvalidRegistrationException {
        throw new InvalidRegistrationException (
                "Agents cannot be registered with a Pasale Game!");
    }

    @Transient
    public String getChangeSet() {
        return "/mx/ecosur/multigame/impl/pasale.xml";
    }

    /* (non-Javadoc)
     * @see GridGame#getColors()
     */
    @Override
    @Transient
    public List<mx.ecosur.multigame.grid.Color> getColors() {
        List<mx.ecosur.multigame.grid.Color> ret = new ArrayList<mx.ecosur.multigame.grid.Color>();
        for (mx.ecosur.multigame.grid.Color color : mx.ecosur.multigame.grid.Color.values()) {
            if (color.equals(mx.ecosur.multigame.grid.Color.UNKNOWN))
                continue;
            if (color.equals(mx.ecosur.multigame.grid.Color.GREEN))
                continue;
            if (color.equals(mx.ecosur.multigame.grid.Color.BLUE))
                continue;
            ret.add(color);
        }

        return ret;
    }

    @Override
    public void setKbase(KnowledgeBase kbase) {
        this.kbase = kbase;
    }    


    @Transient
    public MessageSender getMessageSender() {
        if (messageSender == null) {
            messageSender = new MessageSender ();
            messageSender.initialize();
        }
        return messageSender;
    }

    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }    


    @Transient
    public Resource getResource() {
        return ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
            getChangeSet()));
    }

    @Transient
    public String getGameType() {
        return "Pasale";
    }

    /* (non-Javadoc)
    * @see GridGame#clone()
    */
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        PasaleGame ret = new PasaleGame();
        ret.setPlayers (this.getPlayers());
        ret.setGrid((GameGrid) grid.clone());
        ret.setColumns(this.getColumns());
        ret.setRows (this.getRows());
        ret.setGameType(this.getGameType());
        ret.setState(this.getState());
        ret.setMessageSender(this.getMessageSender());
        ret.setCreated(new Date (System.currentTimeMillis()));
        ret.setId(this.getId());
        ret.setMoves(this.getMoves());
        ret.setVersion(this.getVersion());
        return ret;
    }

    @Override
    public String toString() {
        PasaleGrid tgrid = (PasaleGrid) getGrid();
        StringBuffer ret = new StringBuffer("PasaleGame (id=" + id + ")\n");
        for (int y = 0; y < getColumns(); y++) {
            for (int x = 0; x < getRows(); x++) {
                GridCell cell = grid.getLocation (new GridCell (y,x, mx.ecosur.multigame.grid.Color.UNKNOWN));
                if (cell != null) {
                    PasaleFicha ficha = (PasaleFicha) cell;
                    switch (ficha.getType()) {
                        case SOIL_PARTICLE:
                            ret.append("S");
                            break;
                        case FOREST:
                            if (isDirectlyConnectedToWater(ficha, tgrid.getSquare(ficha)))
                                ret.append("R");
                            else
                                ret.append("F");
                            break;
                        case POTRERO:
                            ret.append("P");
                            break;
                        case SILVOPASTORAL:
                            ret.append("V");
                            break;
                        case WATER_PARTICLE:
                            ret.append("W");
                            break;
                        default:
                            ret.append ("U");
                            break;
                    }
                } else {
                    ret.append(" ");
                }

                /* space out the cells */
                ret.append (" ");
            }
            ret.append("\n");
        }

        return ret.toString();
    }

    public void finalize() {
        try {
            super.finalize();
        } catch (Throwable t) {
           t.printStackTrace();
        }
        
        if (this.session != null)
            session.dispose();
    }
}