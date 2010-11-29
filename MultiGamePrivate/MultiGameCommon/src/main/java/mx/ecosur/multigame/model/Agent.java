/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.model;

import mx.ecosur.multigame.model.implementation.AgentImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;

import java.util.LinkedHashSet;
import java.util.Set;


public class Agent extends GamePlayer {
        
    private static final long serialVersionUID = -6708647912533704063L;

    private AgentImpl implementation;


    public Agent () {
       super();
    }


    public Agent (AgentImpl agentImpl) {
        super (agentImpl);
        this.implementation = agentImpl;
    }

    public void initialize() {
        implementation.initialize();
    }

    public Set<Move> determineMoves (Game game) {
        Set<Move> ret = new LinkedHashSet<Move>();
        Set<MoveImpl> moves = implementation.determineMoves(game.getImplementation());
        for (MoveImpl impl : moves) {
            ret.add(new Move(impl));
        }

        return ret;
    }

    public Suggestion processSuggestion (Game game, Suggestion suggestion) {
        return new Suggestion (implementation.processSuggestion (game.getImplementation(), suggestion.getImplementation()));
    }

    public AgentImpl getImplementation() {
        return implementation;
    }

    public void setImplementation(AgentImpl implementation) {
        this.implementation = implementation;
    }

    public boolean ready () {
        return this.implementation.ready();
    }
}
