/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.enums.GameState;

import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.MoveComparator;
import mx.ecosur.multigame.grid.entity.*;

import mx.ecosur.multigame.impl.enums.manantiales.ConditionType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;

import mx.ecosur.multigame.impl.util.manantiales.AdjGraph;
import mx.ecosur.multigame.impl.util.manantiales.ManantialesMessageSender;
import mx.ecosur.multigame.model.interfaces.*;
import mx.ecosur.multigame.MessageSender;

import javax.persistence.*;

import org.drools.builder.*;
import org.drools.io.Resource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.io.ResourceFactory;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBase;

import java.awt.*;
import java.util.*;
import java.net.MalformedURLException;
import java.util.List;

@Entity
public class ManantialesGame extends GridGame {

    private static final long serialVersionUID = -8395074059039838349L;

    private static transient KnowledgeBase kbase;
    
    private Mode mode;
        
    private Set<CheckCondition> checkConditions;

    private transient ManantialesMessageSender messageSender;

    private Set<PuzzleSuggestion> suggestions;

    private transient AdjGraph graph;

    private Color[] colors = { Color.YELLOW, Color.PURPLE, Color.RED, Color.BLACK,  };

    public ManantialesGame () {
        super();
        mode = Mode.CLASSIC;
        messageSender = new ManantialesMessageSender();
    }

    public ManantialesGame (Mode mode) {
        this();
        this.mode = mode;
    }

    public void initialize() throws MalformedURLException {
        setGrid(new GameGrid());
        setCreated(new Date());
        setColumns(9);
        setRows(9);
        setState(GameState.PLAY);
        if (mode == null)
            mode = Mode.CLASSIC;
        messageSender.initialize();
        messageSender.sendStartGame(this);
    }

    private AdjGraph createGraph() {
        AdjGraph ret = new AdjGraph(48);
        int counter = 0;
        for (int column = 0; column < getColumns(); column++) {
            for (int row = 0; row < getRows(); row++) {
                if (row == 4 && column == 4)
                    continue;

                if ((row % 2 == 0 && column % 2 == 0) ||
                        (row % 2 != 0 && column % 2 != 0) ||
                        (row == 4 || column == 4)) {
                    Point pt = new Point(row, column);
                    ret.addPoint(counter++, pt);
                }
            }
        }

        assert (counter == 48);

        /* Edges only valid if _nRows = 9 and _nCols = 9, e.g. 8x8 grid ; */

        ret.addEdge(0, 1);
        ret.addEdge(0, 5);
        ret.addEdge(0, 10);
        ret.addEdge(1, 5);
        ret.addEdge(1, 6);
        ret.addEdge(1, 2);
        ret.addEdge(2, 6);
        ret.addEdge(2, 7);
        ret.addEdge(2, 8);
        ret.addEdge(2, 3);
        ret.addEdge(3, 8);
        ret.addEdge(3, 9);
        ret.addEdge(3, 4);
        ret.addEdge(4, 9);
        ret.addEdge(4, 14);
        ret.addEdge(5, 10);
        ret.addEdge(5, 11);
        ret.addEdge(5, 6);
        ret.addEdge(5, 15);
        ret.addEdge(6, 11);
        ret.addEdge(6, 16);
        ret.addEdge(6, 12);
        ret.addEdge(6, 7);
        ret.addEdge(7, 12);
        ret.addEdge(7, 8);
        ret.addEdge(8, 12);
        ret.addEdge(8, 18);
        ret.addEdge(8, 13);
        ret.addEdge(8, 9);
        ret.addEdge(9, 13);
        ret.addEdge(9, 19);
        ret.addEdge(9, 14);
        ret.addEdge(10, 20);
        ret.addEdge(10, 15);
        ret.addEdge(11, 15);
        ret.addEdge(11, 16);
        ret.addEdge(12, 16);
        ret.addEdge(12, 17);
        ret.addEdge(12, 18);
        ret.addEdge(13, 18);
        ret.addEdge(13, 19);
        ret.addEdge(14, 19);
        ret.addEdge(14, 27);
        ret.addEdge(15, 16);
        ret.addEdge(15, 20);
        ret.addEdge(15, 21);
        ret.addEdge(15, 22);
        ret.addEdge(16, 22);
        ret.addEdge(16, 23);
        ret.addEdge(16, 17);
        ret.addEdge(17, 23);
        ret.addEdge(17, 24);
        ret.addEdge(17, 18);
        ret.addEdge(18, 24);
        ret.addEdge(18, 25);
        ret.addEdge(18, 19);
        ret.addEdge(19, 25);
        ret.addEdge(19, 26);
        ret.addEdge(19, 27);
        ret.addEdge(20, 33);
        ret.addEdge(20, 28);
        ret.addEdge(20, 21);
        ret.addEdge(21, 22);
        ret.addEdge(21, 28);
        ret.addEdge(22, 28);
        ret.addEdge(22, 29);
        ret.addEdge(22, 23);
        ret.addEdge(23, 29);
        ret.addEdge(23, 30);
        ret.addEdge(24, 30);
        ret.addEdge(24, 31);
        ret.addEdge(24, 25);
        ret.addEdge(25, 31);
        ret.addEdge(25, 32);
        ret.addEdge(25, 26);
        ret.addEdge(26, 32);
        ret.addEdge(26, 27);
        ret.addEdge(27, 32);
        ret.addEdge(27, 37);
        ret.addEdge(28, 33);
        ret.addEdge(28, 34);
        ret.addEdge(28, 38);
        ret.addEdge(28, 29);
        ret.addEdge(29, 34);
        ret.addEdge(29, 39);
        ret.addEdge(29, 35);
        ret.addEdge(29, 30);
        ret.addEdge(30, 35);
        ret.addEdge(30, 35);
        ret.addEdge(30, 31);
        ret.addEdge(31, 35);
        ret.addEdge(31, 41);
        ret.addEdge(31, 36);
        ret.addEdge(31, 32);
        ret.addEdge(32, 36);
        ret.addEdge(32, 42);
        ret.addEdge(32, 37);
        ret.addEdge(33, 43);
        ret.addEdge(33, 38);
        ret.addEdge(34, 38);
        ret.addEdge(34, 39);
        ret.addEdge(35, 39);
        ret.addEdge(35, 40);
        ret.addEdge(35, 31);
        ret.addEdge(35, 41);
        ret.addEdge(36, 41);
        ret.addEdge(36, 42);
        ret.addEdge(37, 42);
        ret.addEdge(37, 47);
        ret.addEdge(38, 43);
        ret.addEdge(38, 44);
        ret.addEdge(38, 39);
        ret.addEdge(39, 44);
        ret.addEdge(39, 45);
        ret.addEdge(39, 40);
        ret.addEdge(40, 41);
        ret.addEdge(40, 45);
        ret.addEdge(41, 45);
        ret.addEdge(41, 46);
        ret.addEdge(41, 42);
        ret.addEdge(42, 46);
        ret.addEdge(42, 47);
        ret.addEdge(43, 44);
        ret.addEdge(44, 45);
        ret.addEdge(45, 46);
        ret.addEdge(46, 47);

        return ret;
    }

    @Override
    @Transient
    public Set getFacts() {
        Set facts = new HashSet();

        if (!grid.isEmpty()) {
            for (GridCell cell : grid.getCells()) {
                ManantialesFicha ficha = (ManantialesFicha) cell;
                facts.add(ficha);
            }
        }

        if (checkConditions != null)
            facts.addAll(checkConditions);
        if (graph == null)
            graph = createGraph();
        facts.add(graph);
        return facts;
    }

    @Override
    @Transient
    public List<Color> getColors() {
        List<Color> ret = new ArrayList<Color>();
        for (Color color : colors) {
            ret.add(color);
        }
        return ret;
    }

    @Transient
    public String getChangeSet() {
        return "/mx/ecosur/multigame/impl/manantiales.xml";
    }

    @Enumerated (EnumType.STRING)
    public Mode getMode() {
        return mode;
    }

    public void setMode (Mode mode) {
        this.mode = mode;
    }

    @OneToMany (cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    public Set<CheckCondition> getCheckConditions () {
        if (checkConditions == null) {
            checkConditions = new HashSet<CheckCondition>();
        }
        return checkConditions;
    }
    
    public void setCheckConditions (Set<CheckCondition> checkConstraints) {
        this.checkConditions = checkConstraints;
    }

    @Transient
    public boolean hasCondition (ConditionType type) {
        boolean ret = false;
        if (checkConditions != null) {
            for (Condition c : checkConditions) {
                CheckCondition condition = (CheckCondition) c;
                if (condition.getType().equals(type)) {
                    ret = true;}}}
        return ret;
    }

    @Transient
    @Override
    public MessageSender getMessageSender() {
        if (messageSender == null) {
            messageSender = new ManantialesMessageSender ();
            messageSender.initialize();
        }
        return messageSender;
    }

    @Override
    public void setMessageSender(MessageSender messageSender) {
        if (messageSender instanceof ManantialesMessageSender)
            this.messageSender = (ManantialesMessageSender) messageSender;
    }

    @OneToMany (cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    public Set<PuzzleSuggestion> getSuggestions () {
        if (suggestions == null)
            suggestions = new HashSet<PuzzleSuggestion>();
        return suggestions;

    }

    public void setSuggestions (Set<PuzzleSuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public void updateSuggestion (PuzzleSuggestion suggestion) {
        if (suggestions != null) {
            for (PuzzleSuggestion possible : suggestions) {
                if (possible.equals(suggestion)) {
                    possible.setStatus(suggestion.getStatus());
                }
            }
        }
    }

    @Transient
    public Resource getResource() {
        return ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
            getChangeSet()));
    }

    @Transient
    public String getGameType() {
        return "Manantiales";
    }

    protected KnowledgeBase findKBase () {
        KnowledgeBase ret = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
            "/mx/ecosur/multigame/impl/manantiales.xml")), ResourceType.CHANGE_SET);
        ret.addKnowledgePackages(kbuilder.getKnowledgePackages());
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (!errors.isEmpty()) {
            for (KnowledgeBuilderError error : errors) {
                System.out.println(error.getMessage());
                System.out.println("@" + error.getErrorLines());
            }
        }

        return ret;
    }

    @Transient
    public int getMaxPlayers() {
        return 4;
    }

    @Override
    public Move move(Move impl) throws InvalidMoveException {
        ManantialesMove move = (ManantialesMove) impl;
        if (move.getMode()== null)
            move.setMode(getMode());

        if (kbase == null)
            kbase = findKBase();

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        session.setGlobal("messageSender", getMessageSender());
        session.insert(this);
        session.insert(move);
        for (Object fact : getFacts()) {
            session.insert(fact);
        }

        session.getAgenda().getAgendaGroup("verify").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("move").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("evaluate").setFocus();
        session.fireAllRules();
        
        session.dispose();

        if (getMoves() == null)
            setMoves(new TreeSet<GridMove>(new MoveComparator()));
        getMoves().add(move);
        
        return move;
    }

    @Override
    public Suggestion suggest (Suggestion suggestion) {
        if (kbase == null)
            kbase = findKBase();
        ManantialesMove move = (ManantialesMove) suggestion.listMove();
        if (move.getMode() == null)
            move.setMode(getMode());

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        session.setGlobal("messageSender", getMessageSender());
        session.insert(this);
        session.insert(suggestion);
        for (Object fact : getFacts())
            session.insert(fact);
        Set<PuzzleSuggestion> set = getSuggestions();
        if (set == null)
            set = new LinkedHashSet<PuzzleSuggestion>();
        set.add((PuzzleSuggestion) suggestion);
        setSuggestions(set);
        session.getAgenda().getAgendaGroup("verify").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("move").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("evaluate").setFocus();
        session.fireAllRules();
        session.dispose();
        return suggestion;
    }

    public GamePlayer registerPlayer(Registrant registrant) throws InvalidRegistrationException  {
        if (getPlayers() == null)
            setPlayers(new TreeSet<GridPlayer>());
        List<Color> colors = getAvailableColors();
        ManantialesPlayer player = new ManantialesPlayer((GridRegistrant) registrant, colors.get(0));
        for (GridPlayer p : this.getPlayers()) {
            if (p.equals (player))
                throw new InvalidRegistrationException ("Duplicate Registraton!");
        }
        int max = getMaxPlayers();
        if (players.size() == max)
            throw new RuntimeException ("Maximum Players reached!");
        if (player.getColor().equals(Color.YELLOW))
            player.setTurn(true);
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
        
    public Agent registerAgent (Agent agent) throws InvalidRegistrationException {
        if (agent instanceof SimpleAgent) {
            SimpleAgent player = (SimpleAgent) agent;

            if (getPlayers () != null) {
                for (GridPlayer p : this.getPlayers()) {
                    if (p.equals (player))
                        throw new InvalidRegistrationException ("Duplicate Registraton!");
                }
                
                int max = getMaxPlayers();
                if (players.size() == max)
                    throw new RuntimeException ("Maximum Players reached!");
            }

            List<Color> colors = getAvailableColors();
            player.setColor(colors.get(0));
            if (player.getColor().equals(Color.YELLOW))
                player.setTurn(true);
            if (players == null)
                players = new LinkedHashSet<GridPlayer>();
            players.add(player);
            if (players.size() == getMaxPlayers())
            try {
                initialize();
            } catch (MalformedURLException e) {
                throw new InvalidRegistrationException (e);
            }

        } else {
            throw new InvalidRegistrationException("Unknown Agent type! [" + agent.getClass() + "]");
        }

        if (this.created == null)
            this.setCreated(new Date());
        if (this.state == null)
            this.state = GameState.WAITING;

        return agent;
    }

    @Override
    public String toString() {
        return super.toString();
        //return getMode() + ":\n" + getGrid().toString();
    }
}
