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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;

import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mx.ecosur.multigame.enums.GameState;

import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.GridGame;

import mx.ecosur.multigame.model.implementation.GamePlayerImpl;
import mx.ecosur.multigame.model.implementation.MoveImpl;
import mx.ecosur.multigame.model.implementation.RegistrantImpl;


@NamedQueries( {
	@NamedQuery(name = "getPenteGame", query = "select g from GenteGame g where g.state =:state"),
	@NamedQuery(name = "getPenteGameById", query = "select g from GenteGame g where g.id=:id ") })
@Entity
public class GenteGame extends GridGame {
	
	private static final long serialVersionUID = -4437359200244786305L;
	
	private Set<GentePlayer> winners;
	
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

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Game#getMaxPlayers()
	 */
	@Override
	public int getMaxPlayers() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#initialize()
	 */
	public void initialize() {
	    this.setCreated(new Date());
	    this.setState(GameState.BEGIN);
		this.setColumns(9);
		this.setRows(9);	  
		RuleBase ruleBase = null;
		
		if (ruleBase == null) {
			PackageBuilder builder = new PackageBuilder();
		    InputStreamReader reader = new InputStreamReader(
		    		getClass().getResourceAsStream(
		    				"/mx/ecosur/multigame/impl/gente.drl"));
		    try {
				builder.addPackageFromDrl(reader);
			} catch (DroolsParserException e) {
				e.printStackTrace();
				throw new RuntimeException (e);
			} catch (IOException e) {			
				e.printStackTrace();
				throw new RuntimeException (e);
			}
		    ruleBase = RuleBaseFactory.newRuleBase();
		    ruleBase.addPackage(builder.getPackage());
		}			
		
		StatefulSession session = ruleBase.newStatefulSession();
		session.insert(this);
		for (Object fact : getFacts()) {
			session.insert(fact);
		}
		session.setFocus("initialize");
		session.fireAllRules();
		session.dispose();
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#move(mx.ecosur.multigame.model.implementation.MoveImpl)
	 */
	@Override
	public void move(MoveImpl move) throws InvalidMoveException {
		RuleBase ruleBase = null;
		
		try {
			if (ruleBase == null) {
				PackageBuilder builder = new PackageBuilder();
			    InputStreamReader reader = new InputStreamReader(
			    		getClass().getResourceAsStream(
			    				"/mx/ecosur/multigame/impl/gente.drl"));
			    builder.addPackageFromDrl(reader);
			    ruleBase = RuleBaseFactory.newRuleBase();
			    ruleBase.addPackage(builder.getPackage());
			}
			
			/* Make the move */
			StatefulSession session = ruleBase.newStatefulSession();
			session.insert(this);
			session.insert(move);
			for (Object fact : getFacts()) {
				session.insert(fact);
			}
			
			session.setFocus("verify");
		    session.fireAllRules();
		    session.setFocus("move");
		    session.fireAllRules();
		    session.setFocus("evaluate");
		    session.fireAllRules();
		    session.dispose();			
			
		} catch (Exception e) {
			throw new InvalidMoveException (e.getMessage());
		}										
	}
	
	public GamePlayerImpl registerPlayer(RegistrantImpl registrant)  {			
		GentePlayer player = new GentePlayer ();
		player.setPlayer(registrant);
		player.setGame(this);	
		
		int max = getMaxPlayers();
		if (players.size() == max)
			throw new RuntimeException ("Maximum Players reached!");
		
		List<Color> colors = getAvailableColors();
		player.setColor(colors.get(0));		
		players.add(player);
		
		if (players.size() == getMaxPlayers())
			initialize();		
		
		/* Be sure that the player has a good reference to this game */
		player.setGame(this);
		
		return player;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#getColors()
	 */
	@Override
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
}
