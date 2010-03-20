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

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.*;

import mx.ecosur.multigame.impl.enums.manantiales.ConditionType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;

import mx.ecosur.multigame.impl.util.manantiales.ManantialesMessageSender;
import mx.ecosur.multigame.model.implementation.*;
import mx.ecosur.multigame.MessageSender;

import javax.persistence.*;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.io.ResourceFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBase;

import java.util.*;
import java.net.MalformedURLException;

@Entity
public class ManantialesGame extends GridGame {
        
    private static final long serialVersionUID = -8395074059039838349L;
        
    private Mode mode;
        
    private Set<CheckCondition> checkConditions;

    private transient ManantialesMessageSender messageSender;

    private Set<PuzzleSuggestion> suggestions;


    public ManantialesGame () {
        super();
    }

    public ManantialesGame (KnowledgeBase kbase) {
        this.kbase = kbase;
    }
    
    @Enumerated (EnumType.STRING)
    public Mode getMode() {
        return mode;
    }

    public void setMode (Mode mode) {
        this.checkConditions = null;
        this.mode = mode;
    }
    
    public boolean hasCondition (ConditionType type) {
        boolean ret = false;
        if (checkConditions != null) {
            for (CheckCondition condition : checkConditions) {
                if (condition.getType().equals(type)) {
                    ret = true;
                }
            }
        }

        return ret;
    }

    @OneToMany (cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
    public Set<CheckCondition> getCheckConditions () {
        if (checkConditions == null)
                checkConditions = new HashSet<CheckCondition>();
        return checkConditions;
    }
    
    public void setCheckConditions (Set<CheckCondition> checkConstraints) {
        this.checkConditions = checkConstraints;
    }
    
    public void addCheckCondition (CheckCondition violation) {
        if (checkConditions == null) 
                checkConditions = new HashSet<CheckCondition>();
        if (!hasCondition (ConditionType.valueOf(violation.getReason())))
                checkConditions.add(violation);
    }

    public void addSuggestion (PuzzleSuggestion suggestion) {
        if (suggestions == null)
            suggestions = new LinkedHashSet<PuzzleSuggestion>();
        
        if (suggestions.contains (suggestion))
            updateSuggestion(suggestion);
        else
            suggestions.add(suggestion);
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

    public void addMove (ManantialesMove move) {
        if (moves != null)
            moves.add(move);
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.model.Game#getFacts()
     */
    @Override
    @Transient
    public Set<Implementation> getFacts() {
        Set<Implementation> facts = super.getFacts();
        if (checkConditions != null)
            facts.addAll(checkConditions);
        if (suggestions != null)
            facts.addAll(suggestions);
        return facts;
    }


    /* (non-Javadoc)
     * @see mx.ecosur.multigame.model.Game#initialize(mx.ecosur.multigame.GameType)
     */
    public void initialize() throws MalformedURLException {
        this.setGrid(new GameGrid());
        this.setState(GameState.BEGIN);
        this.setCreated(new Date());
        this.setColumns(9);
        this.setRows(9);

        if (kbase == null) {
            kbase = KnowledgeBaseFactory.newKnowledgeBase();
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                "/mx/ecosur/multigame/impl/manantiales.xml")), ResourceType.CHANGE_SET);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        session.setGlobal("messageSender", getMessageSender());
        session.insert(this);
        for (Object fact : getFacts()) {
            session.insert(fact);
        }

        session.getAgenda().getAgendaGroup("initialize").setFocus();
        session.fireAllRules();
        session.dispose();
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
    @Override
    public MoveImpl move(MoveImpl move) throws InvalidMoveException {
        if (kbase == null) {
            kbase = KnowledgeBaseFactory.newKnowledgeBase();
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                "/mx/ecosur/multigame/impl/manantiales.xml")), ResourceType.CHANGE_SET);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        session.setGlobal("messageSender", getMessageSender()); 
        session.insert(this);
        session.insert(move);
        for (Implementation fact : getFacts()) {
            session.insert(fact);
        }

        session.getAgenda().getAgendaGroup("verify").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("move").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("evaluate").setFocus();
        session.fireAllRules();
        session.dispose();

        if (moves == null)
            moves = new LinkedHashSet<GridMove>();

        moves.add((ManantialesMove) move);

        return move;
    }

    @Override
    public SuggestionImpl suggest (SuggestionImpl suggestion) {
        if (kbase == null) {
            kbase = KnowledgeBaseFactory.newKnowledgeBase();
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                "/mx/ecosur/multigame/impl/manantiales.xml")), ResourceType.CHANGE_SET);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }

        addSuggestion ((PuzzleSuggestion) suggestion);

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        session.setGlobal("messageSender", getMessageSender());
        session.insert(this);
        for (Implementation fact : getFacts()) {
            session.insert(fact);
        }

        session.getAgenda().getAgendaGroup("verify").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("move").setFocus();
        session.fireAllRules();
        session.getAgenda().getAgendaGroup("evaluate").setFocus();
        session.fireAllRules();
        session.dispose();

        return suggestion;
    }
        
    public GamePlayerImpl registerPlayer(RegistrantImpl registrant) throws InvalidRegistrationException  {
        ManantialesPlayer player = new ManantialesPlayer ();
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

        if (this.created == 0)
            this.setCreated(new Date());
        if (this.state == null)
                this.state = GameState.WAITING;

        return player;
    }
        
    public AgentImpl registerAgent (AgentImpl agent) throws InvalidRegistrationException {
        SimpleAgent player = (SimpleAgent) agent;

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

        return player;        
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
            messageSender = new ManantialesMessageSender ();
            messageSender.initialize();
        }
        return messageSender;
    }

    public void setMessageSender(MessageSender messageSender) {
        if (messageSender instanceof ManantialesMessageSender)
            this.messageSender = (ManantialesMessageSender) messageSender;
    }

    @OneToMany (cascade=CascadeType.PERSIST, fetch=FetchType.EAGER)
    public Set<PuzzleSuggestion> getSuggestions () {
        return suggestions;

    }

    public void setSuggestions (Set<PuzzleSuggestion> suggestions) {
        this.suggestions = suggestions;
    }


    @Transient
    public Set<PuzzleSuggestion> findSuggestion (ManantialesMove move) {
        Set<PuzzleSuggestion> ret = new LinkedHashSet<PuzzleSuggestion>();
        for (PuzzleSuggestion suggestion : suggestions) {
            if (suggestion.getMove().equals(move))
                ret.add(suggestion);
        }


        return ret;
    }


    @Transient
    public String getGameType() {
        return "Manantiales";
    }

    public void setGameType (String type) {
        // do nothing;
    }

    @Override
    public String toString() {
        return getGrid().toString();
    }/* (non-Javadoc)
     * @see mx.ecosur.multigame.impl.model.GridGame#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ManantialesGame ret = new ManantialesGame();
        ret.setGrid((GameGrid) grid.clone());
        ret.setMode(this.getMode());
        return ret;
    }
}
