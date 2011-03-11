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
package mx.ecosur.multigame.impl.entity.gente;

import javax.persistence.*;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.MoveComparator;
import mx.ecosur.multigame.grid.model.*;
import mx.ecosur.multigame.model.interfaces.Agent;
import mx.ecosur.multigame.model.interfaces.GamePlayer;
import mx.ecosur.multigame.model.interfaces.Move;
import org.drools.*;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;

import org.drools.runtime.StatefulKnowledgeSession;

import java.io.*;
import java.util.*;
import java.net.MalformedURLException;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.model.interfaces.Registrant;
import mx.ecosur.multigame.MessageSender;


@Entity
public class GenteGame extends GridGame {
        
    private static final long serialVersionUID = -4437359200244786305L;
        
    private Set<GentePlayer> winners;

    private transient MessageSender messageSender;

    private transient KnowledgeBase kbase;

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

    static class PlayerComparator implements Serializable, Comparator <GentePlayer>{

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
      * @see GridGame#move(mx.ecosur.multigame.model.interfaces.Move)
      */
    public Move move(Move move) throws InvalidMoveException {
        if (kbase == null)
            kbase = findKBase();

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
            moves = new TreeSet<GridMove>(new MoveComparator());
        moves.add((GenteMove) move);
        return move;
    }
        
    public GamePlayer registerPlayer(Registrant registrant) throws
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

    public Agent registerAgent (Agent agent) throws InvalidRegistrationException {
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

    @Override
    public List<GamePlayer> listPlayers() {
      List<GamePlayer> ret = new ArrayList<GamePlayer>();
      for (GridPlayer p : players) {
          ret.add(p);
      }

      return ret;
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
     * @see GridGame#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        GenteGame ret = new GenteGame ();
        ret.grid = new GameGrid();
        for (GridCell cell : getGrid().getCells()) {
            ret.grid.updateCell((GridCell) cell.clone());
        }
        ret.setColumns (this.getColumns());
        ret.setRows (this.getRows());

        ret.created = System.currentTimeMillis();
        ret.id = this.getId();
        ret.moves = new TreeSet<GridMove>(new MoveComparator());
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
        if (kbase == null)
            kbase = findKBase();
        ret.kbase = kbase;

        for (GentePlayer winner : getWinners()) {
                GentePlayer clone = (GentePlayer) winner.clone();
                ret.winners.add(clone);
        }

        return ret;
    }

    protected KnowledgeBase findKBase () {
        KnowledgeBase ret = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
            "/mx/ecosur/multigame/impl/gente.xml")), ResourceType.CHANGE_SET);
        ret.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return ret;
    }

    @Transient
    public void setKbase(KnowledgeBase kbase) {
        this.kbase = kbase;
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
