package mx.ecosur.multigame.impl.entity.tablon;

import mx.ecosur.multigame.enums.GameState;

import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.*;

import mx.ecosur.multigame.model.implementation.AgentImpl;
import mx.ecosur.multigame.model.implementation.GamePlayerImpl;
import mx.ecosur.multigame.model.implementation.Implementation;
import mx.ecosur.multigame.model.implementation.MoveImpl;
import mx.ecosur.multigame.model.implementation.RegistrantImpl;
import static mx.ecosur.multigame.impl.util.tablon.RuleFunctions.*;
import mx.ecosur.multigame.MessageSender;

import javax.persistence.*;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.io.ResourceFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBase;
import org.drools.audit.WorkingMemoryFileLogger;

import java.util.*;
import java.net.MalformedURLException;

@Entity
public class TablonGame extends GridGame {
	
	private static final long serialVersionUID = -8395074059039838349L;

    private static final String ChangeSet = "/mx/ecosur/multigame/impl/tablon.xml";

    private static final boolean DEBUG = false;
	
	private transient MessageSender messageSender;

    private StatefulKnowledgeSession session;

    private WorkingMemoryFileLogger logger;

    public  TablonGame () {
        super();
    }


    public TablonGame(int columns, int rows) {
        super();
        setColumns(columns);
        setRows(rows);        
    }

    public TablonGame(int columns, int rows, KnowledgeBase kbase) {
        this(columns, rows);
        this.kbase = kbase;
    }

    /* (non-Javadoc)
      * @see mx.ecosur.multigame.model.Game#initialize(mx.ecosur.multigame.GameType)
      */
    public void initialize() throws MalformedURLException {
        this.setState(GameState.BEGIN);
        if (kbase == null) {
            kbase = KnowledgeBaseFactory.newKnowledgeBase();
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                ChangeSet)), ResourceType.CHANGE_SET);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }

        if (session == null) {
            session = kbase.newStatefulKnowledgeSession();
            session.setGlobal("messageSender", getMessageSender());
            session.setGlobal("dimension", new Integer(this.getColumns()));
            session.insert(this);
            for (Implementation fact : getFacts()) {
                session.insert(fact);
            }
        }

        session.startProcess("tablon-flow");
        session.fireAllRules();
    }

    @Transient
    public int getMaxPlayers() {
        return 4;
    }

    public void setMaxPlayers(int maxPlayers) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
      * @see mx.ecosur.multigame.impl.model.GridGame#move(mx.ecosur.multigame.model.implementation.MoveImpl)
      */
    public MoveImpl move(MoveImpl move) throws InvalidMoveException {
        if (kbase == null) {
            kbase = KnowledgeBaseFactory.newKnowledgeBase();
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                ChangeSet)), ResourceType.CHANGE_SET);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }
        
        if (session == null) {
            session = kbase.newStatefulKnowledgeSession();
            session.setGlobal("messageSender", getMessageSender());
            session.setGlobal("dimension", new Integer(getColumns()));
            session.insert(this);            
        }

        if (logger == null && DEBUG) {
            logger = new WorkingMemoryFileLogger(session);
            logger.setFileName("audit");
        }

        session.insert(move);
        session.startProcess("tablon-flow");
        session.fireAllRules();
        if (logger != null)
            logger.writeToDisk();

        if (moves == null)
            moves = new LinkedHashSet<GridMove>();

        moves.add((TablonMove) move);

        return move;
    }
	
	public GamePlayerImpl registerPlayer(RegistrantImpl registrant) throws InvalidRegistrationException  {			
		TablonPlayer player = new TablonPlayer();
		player.setRegistrant((GridRegistrant) registrant);
		
		for (GridPlayer p : this.getPlayers()) {
			if (p.equals (player))
				throw new InvalidRegistrationException ("Duplicate Registraton!");
		}		
		
		int max = getMaxPlayers();
		if (players.size() == max)
			throw new RuntimeException ("Maximum Players reached!");
		
		List<Color> colors = getAvailableColors();
		player.setColor(colors.get(0));		
		players.add(player);

        try {
		    if (players.size() == getMaxPlayers())
			    initialize();
        } catch (MalformedURLException e) {
            throw new InvalidRegistrationException (e);
        }
		
		if (this.created == null)
		    this.setCreated(new Date());	
		if (this.state == null)
			this.state = GameState.WAITING;
		
		return player;
	}
	
	public AgentImpl registerAgent (AgentImpl agent) throws InvalidRegistrationException {
		throw new InvalidRegistrationException (
				"Agents cannot be registered with an Oculto Game!");
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#getColors()
	 */
	@Override
    @Transient
	public List<Color> getColors() {
		List<Color> ret = new ArrayList<Color>();
		for (Color color : Color.values()) {
			if (color.equals(Color.UNKNOWN))
				continue;
			if (color.equals(Color.GREEN))
				continue;
			ret.add(color);
		}
		
		return ret;
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
    public String getGameType() {
        return "Tablon";
    }

    public void setGameType (String type) {
       type = type;
    }    

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public String toString() {
        TablonGrid tgrid = (TablonGrid) getGrid();
        StringBuffer ret = new StringBuffer("TablonGame (id=" + id + ")\n");
        for (int y = 0; y < getColumns(); y++) {
            for (int x = 0; x < getRows(); x++) {
                GridCell cell = grid.getLocation (new GridCell (y,x, Color.UNKNOWN));
                if (cell != null) {
                    TablonFicha ficha = (TablonFicha) cell;
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
                            assert (false);
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
        if (this.session != null)
            session.dispose();
    }
}
