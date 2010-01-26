/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * GenteGame extends the general Game object with some Pente (or Gente) specific
 * methods and functionality.  GenteGame provides callers with the winners of 
 * the game it manages to.
 * 
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.gente;

import javax.persistence.*;

import org.drools.*;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.agent.KnowledgeAgent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import java.util.*;
import java.net.MalformedURLException;

import mx.ecosur.multigame.enums.GameState;

import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.*;

import mx.ecosur.multigame.model.implementation.AgentImpl;
import mx.ecosur.multigame.model.implementation.GamePlayerImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;
import mx.ecosur.multigame.model.implementation.RegistrantImpl;
import mx.ecosur.multigame.MessageSender;


@Entity
public class GenteGame extends GridGame {
	
	private static final long serialVersionUID = -4437359200244786305L;
	
	private Set<GentePlayer> winners;

    private transient MessageSender messageSender;

    public GenteGame () {
        super();
    }

    public GenteGame (KnowledgeBase kbase) {
        this.kbase = kbase;
    }
	
	@OneToMany (fetch=FetchType.EAGER)
	public Set <GentePlayer> getWinners () {
		if (winners == null)
			winners = new TreeSet<GentePlayer>(new PlayerComparator());
		return winners;
	}
	
	public void setWinners(Set<GentePlayer> winners){
		this.winners = winners;
	}
	
	class PlayerComparator implements Serializable, Comparator <GentePlayer>{

		private static final long serialVersionUID = 8076875284327150645L;

		public int compare(GentePlayer alice, GentePlayer bob) {
			int ret = 0;
			
			GentePlayer p1 = (GentePlayer) alice, p2 = (GentePlayer) bob;
			if (p1.getPoints() > p2.getPoints())
				ret = 1;
			else if (p1.getPoints() < p2.getPoints())
				ret = -1;
			return ret;	
		}
	}

    @Transient
	public int getMaxPlayers() {
		return 4;
	}


    /* (non-Javadoc)
      * @see mx.ecosur.multigame.impl.model.GridGame#initialize()
      */
    public void initialize() throws MalformedURLException {
        this.setCreated(new Date());
        this.setState(GameState.BEGIN);
        this.setColumns(19);
        this.setRows(19);

        if (kbase == null) {
            kbase = KnowledgeBaseFactory.newKnowledgeBase();
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                "/mx/ecosur/multigame/impl/gente.xml")), ResourceType.CHANGE_SET);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        session.setGlobal("messageSender", getMessageSender());
        session.insert(this);

        session.getAgenda().getAgendaGroup("initialize").setFocus();
        session.fireAllRules();
        session.dispose();
    }

    /* (non-Javadoc)
      * @see mx.ecosur.multigame.impl.model.GridGame#move(mx.ecosur.multigame.model.implementation.MoveImpl)
      */
    public MoveImpl move(MoveImpl move) throws InvalidMoveException {
        if (kbase == null) {
            kbase = KnowledgeBaseFactory.newKnowledgeBase();
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                "/mx/ecosur/multigame/impl/gente.xml")), ResourceType.CHANGE_SET);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }                

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        session.setGlobal("messageSender", getMessageSender());
        session.insert(this);
        session.insert(move);

        session.getAgenda().getAgendaGroup("verify").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("move").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("evaluate").setFocus();
        session.fireAllRules();
        session.dispose();

        if (moves == null)
            moves = new LinkedHashSet<GridMove>();
        moves.add((GenteMove) move);

        return move;
    }
	
	public GamePlayerImpl registerPlayer(RegistrantImpl registrant) throws 
		InvalidRegistrationException  
	{	
		GentePlayer player = new GentePlayer ();
		player.setRegistrant((GridRegistrant) registrant);
		
		for (GridPlayer p : this.getPlayers()) {
			if (p.equals (player))
				throw new InvalidRegistrationException ("Duplicate Registraton!");
		}
		
		int max = getMaxPlayers();
		if (players.size() == max)
			throw new InvalidRegistrationException ("Maximum Players reached!");
		
		List<Color> colors = getAvailableColors();
		player.setColor(colors.get(0));		
		players.add(player);

        try {

		    if (players.size() == getMaxPlayers())
			    initialize();
        } catch (MalformedURLException e) {
            throw new InvalidRegistrationException (e);
        }
		
		if (this.created == 0)
		    this.setCreated(new Date());
		if (this.state == null)
			this.state = GameState.WAITING;
		
		return player;
	}
	
	public AgentImpl registerAgent (AgentImpl agent) throws InvalidRegistrationException {
		GenteStrategyAgent player = (GenteStrategyAgent) agent;
		
		for (GridPlayer p : this.getPlayers()) {
			if (p.equals (player))
				throw new InvalidRegistrationException (
						"Duplicate Registration! " + player.getRegistrant().getName());
		}		
		
		int max = getMaxPlayers();
		if (players.size() == max)
			throw new RuntimeException ("Maximum Players reached!");
		
		List<Color> colors = getAvailableColors();
		player.setColor(colors.get(0));
		players.add(player);
		
		if (players.size() == getMaxPlayers())
            try {
                initialize();
            } catch (MalformedURLException e) {
                throw new InvalidRegistrationException (e);
            }

        if (this.created == 0)
		    this.setCreated(new Date());
		if (this.state == null)
			this.state = GameState.WAITING;
		
		return (AgentImpl) player;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#getColors()
	 */
	@Override
    @Transient
	public List<Color> getColors() {
		List<Color> ret = new ArrayList<Color>();
		for (Color color : Color.values()) {
			if (color.equals(Color.BLACK))
				continue;
			if (color.equals(Color.UNKNOWN))
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
        return "Gente";
    }

    public void setGameType (String type) {
        // do nothing;
    }

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		GenteGame ret = new GenteGame ();
		ret.grid = new GameGrid();
        for (GridCell cell : getGrid().getCells()) {
            ret.grid.updateCell((GridCell) cell.clone());
        }
        ret.setColumns (this.getColumns());
        ret.setRows (this.getRows());

		ret.created = System.currentTimeMillis();
		ret.id = this.getId();
		ret.moves = new LinkedHashSet<GridMove> ();
		for (GridMove move : getMoves()) {
            GenteMove gm = (GenteMove) move;
			ret.moves.add((GridMove) gm.clone());
		}
		
		ret.players = new ArrayList<GridPlayer>();
		for (GridPlayer player : getPlayers()) {
			ret.players.add((GentePlayer) ((GentePlayer) player).clone());
		}
		
		ret.state = this.state;
		ret.version = this.version;
		ret.winners = new LinkedHashSet<GentePlayer>();
        ret.kbase = KnowledgeBaseFactory.newKnowledgeBase();
        if (kbase == null) {
            kbase = KnowledgeBaseFactory.newKnowledgeBase();
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                "/mx/ecosur/multigame/impl/gente.xml")), ResourceType.CHANGE_SET);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }    
        ret.kbase.addKnowledgePackages(kbase.getKnowledgePackages());
		
		for (GentePlayer winner : getWinners()) {
			GentePlayer clone = (GentePlayer) winner.clone();
			ret.winners.add(clone);
		}
		
		return ret;
	}


    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer ("GenteGame\n");
        for (int row = 0; row < this.getRows(); row++) {
            for (int col = 0; col < this.getColumns(); col++) {
                GridCell test = new GridCell (col, row, Color.UNKNOWN);
                GridCell loc = grid.getLocation(test);
                if (loc != null) {
                    buf.append (loc.getColor());
                } else {
                    buf.append ("x");
                }

                buf.append (" ");
            }
            buf.append ("\n");
        }

        return buf.toString();
    }
}
