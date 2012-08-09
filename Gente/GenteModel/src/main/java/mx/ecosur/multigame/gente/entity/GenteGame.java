/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
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
package mx.ecosur.multigame.gente.entity;

import javax.persistence.*;

import mx.ecosur.multigame.exception.InvalidSuggestionException;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.comparator.PlayerComparator;
import mx.ecosur.multigame.grid.entity.*;
import mx.ecosur.multigame.model.interfaces.*;
import org.drools.*;
import org.drools.builder.*;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;

import org.drools.runtime.StatefulKnowledgeSession;

import java.io.*;
import java.util.*;
import java.net.MalformedURLException;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.MessageSender;


@Entity
public class GenteGame extends GridGame {
        
    private static final long serialVersionUID = -4437359200244786305L;
        
    private Set<GentePlayer> winners;

    private transient MessageSender messageSender;

    private static KnowledgeBase kbase;

    private static File Temp;

    public GenteGame () {
        super();
        winners = new LinkedHashSet<GentePlayer>();
    }

    @OneToMany (cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
    @JoinColumn(nullable=true)
    public Set <GentePlayer> getWinners () {
        if (winners == null)
            winners = new TreeSet<GentePlayer>(new GentePlayerComparator());
        return winners;
    }

    public void setWinners(Set<GentePlayer> winners){
        this.winners = winners;
    }

    static class GentePlayerComparator implements Serializable, Comparator <GentePlayer>{

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
      * @see GridGame#initialize()
      */
    public void initialize() throws MalformedURLException {
        this.setCreated(new Date());
        this.setState(GameState.BEGIN);
        this.setColumns(19);
        this.setRows(19);

        if (kbase == null)
            kbase = findKBase();

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        session.setGlobal("messageSender", getMessageSender());
        session.insert(this);

        session.getAgenda().getAgendaGroup("initialize").setFocus();
        session.fireAllRules();
        session.dispose();
    }

    /* (non-Javadoc)
      * @see GridGame#move(mx.ecosur.multigame.entity.interfaces.Move)
      */
    public Move move(Move m) throws InvalidMoveException {
        GridMove move = (GridMove) m;
        if (kbase == null)
            kbase = findKBase();
        if (grid.isEmpty())
            grid.setCells(new TreeSet<GridCell>(new CellComparator()));

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

        /* Add the completed move */
        Set<GridMove> moves = getMoves();
        moves.add((GridMove) move);
        setMoves(moves);

        return move;
    }

    public GamePlayer registerPlayer(Registrant registrant) throws
            InvalidRegistrationException
    {
        if (getPlayers () == null)
            setPlayers(new TreeSet<GridPlayer>(new PlayerComparator()));
        List<Color> colors = getAvailableColors();
        if (colors == null)
            throw new RuntimeException ("No colors available!  Current players: " + players);
        GentePlayer player = new GentePlayer((GridRegistrant) registrant, colors.get(0));
        for (GridPlayer p : this.getPlayers()) {
            if (p.equals (player))
                return p;
        }
        int max = getMaxPlayers();
        if (players.size() == max)
            throw new InvalidRegistrationException ("Maximum Players reached!");
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

    @Override
    public Suggestion suggest(Suggestion suggestion) throws InvalidSuggestionException {
        throw new InvalidSuggestionException("Suggest not supported in Gente!");
    }

    public Agent registerAgent (Agent agent) throws InvalidRegistrationException {
        GenteStrategyAgent player = (GenteStrategyAgent) agent;
        if (getPlayers () == null)
            setPlayers(new TreeSet<GridPlayer>(new PlayerComparator()));

        for (GridPlayer p : this.getPlayers()) {
            if (p.equals (player))
                throw new InvalidRegistrationException (
                    "Duplicate Registration! " + player.getName());
        }

        int max = getMaxPlayers();
        if (players.size() == max)
            throw new RuntimeException ("Maximum Players reached!");

        List<Color> colors = getAvailableColors();
        if (colors == null)
            throw new RuntimeException ("No colors available! Current players: " + players);

        player.setColor(colors.get(0));
        players.add(player);

        if (players.size() == getMaxPlayers())
        try {
            initialize();
        } catch (MalformedURLException e) {
            throw new InvalidRegistrationException (e);
        }

        if (this.created == null)
            this.setCreated(new Date());
        if (this.state == null)
            this.state = GameState.WAITING;

        return (Agent) player;
    }

    /* (non-Javadoc)
     * @see GridGame#getColors()
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
            if (color.equals(Color.PURPLE))
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

    protected KnowledgeBase findKBase () {
        KnowledgeBase ret = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        Resource resource = ResourceFactory.newInputStreamResource(
                this.getClass().getResourceAsStream ("/mx/ecosur/multigame/gente/gente.xml"));
        kbuilder.add(resource, ResourceType.CHANGE_SET);
        ret.addKnowledgePackages(kbuilder.getKnowledgePackages());
        try {
            resource.getReader().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (!errors.isEmpty()) {
            for (KnowledgeBuilderError error : errors) {
                System.out.println(error.getMessage());
            }
        }

        return ret;
    }

    @Transient
    public void setKbase(KnowledgeBase kbase) {
        // do nothing
    }

    @Transient
    public String getChangeSet() {
        return "/mx/ecosur/multigame/impl/gente.xml";
    }

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
