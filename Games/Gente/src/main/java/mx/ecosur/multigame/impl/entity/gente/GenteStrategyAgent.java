/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/
/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.gente;

import java.util.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.enums.Direction;
import mx.ecosur.multigame.grid.model.GameGrid;
import mx.ecosur.multigame.grid.model.GridCell;
import mx.ecosur.multigame.grid.model.GridPlayer;
import mx.ecosur.multigame.grid.model.GridRegistrant;
import mx.ecosur.multigame.grid.util.Search;
import mx.ecosur.multigame.model.interfaces.Game;
import mx.ecosur.multigame.model.interfaces.Move;
import mx.ecosur.multigame.model.interfaces.Suggestion;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;

import mx.ecosur.multigame.grid.Color;

import mx.ecosur.multigame.impl.enums.gente.GenteStrategy;

import mx.ecosur.multigame.model.interfaces.Agent;

import static mx.ecosur.multigame.impl.util.gente.AgentRuleFunctions.*;

@Entity
public class GenteStrategyAgent extends GentePlayer implements Agent {
        
    private static final long serialVersionUID = 6999849272112074624L;

    private KnowledgeBase kBase;

    private GenteStrategy strategy;

    public GenteStrategyAgent () {
        super();
    }

    public GenteStrategyAgent (GridRegistrant player, Color color, GenteStrategy strategy)
    {
        super (player, color);
        this.strategy = strategy;
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.model.interfaces.Agent#initialize()
     */
    public void initialize() {
        kBase = strategy.getRuleBase();
    }

    public boolean ready() {
        return isTurn();
    }

    @Enumerated (EnumType.STRING)
    public GenteStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(GenteStrategy strategy) {
        this.strategy = strategy;
    }

    public Set<Move> determineMoves (Game impl) {
        Set<Move> ret = new LinkedHashSet<Move>();
        GenteGame game = (GenteGame) impl;
        GameGrid grid = game.getGrid();
        
        if (isTurn()) {
            if (kBase == null)
                initialize();
            StatefulKnowledgeSession session = kBase.newStatefulKnowledgeSession();
            session.setGlobal("dim", game.getSize());
            session.insert(this);
            session.insert(ret);
            session.insert(grid);

            /* Insert any unbound adjacent cells if grid is not empty */
            if (!grid.isEmpty()) {
                Set<Color> colors = new HashSet<Color>();
                colors.add(getColor());
                colors.add(getColor().getCompliment());

                for (GridCell cell : findUnboundAdjacentCells(game, colors)) {
                    session.insert(cell);
                }


                /* Bind cells adjacent to enemy team colors to global var for worst case */
                colors.clear();
                for (GridPlayer p : game.getPlayers()) {
                    if (p.getColor().equals(getColor()))
                        continue;
                    else if (p.getColor().equals(getColor().getCompliment()))
                        continue;
                    colors.add(p.getColor());
                }

                Set<GridCell> unbound = findUnboundAdjacentCells(game, colors);
                session.setGlobal("unbound", unbound);

            } else {
                session.setGlobal("unbound", Collections.EMPTY_SET);
            }

            session.fireAllRules();
            session.dispose();
        }

        return ret;
    }

    public Suggestion processSuggestion (Game game, Suggestion suggestion) {
        throw new RuntimeException ("No Suggestions allowed in Gente!");
    }
    
    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer ();
        ret.append ("Agent color: " + super.getColor() + ", isTurn? " + isTurn() + ", strategy: " +
                this.strategy.toString());
        return ret.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = super.equals(obj);
        if (obj instanceof GenteStrategyAgent) {
            GenteStrategyAgent comp = (GenteStrategyAgent) obj;
            ret = ret && comp.getStrategy().equals(this.getStrategy());
        }
        return ret;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).append(getColor()).append(strategy).toHashCode();
    }
}
