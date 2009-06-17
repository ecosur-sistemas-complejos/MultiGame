/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Enum for articulating accessible games in our prototype.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.Type;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;

public enum GameType implements Type {

	PENTE, MANANTIALES;

	private RuleBase ruleBase;

	private static Logger logger = Logger.getLogger(GameType.class
			.getCanonicalName());

	public String getNamedQuery() {
		if (this.equals(GameType.PENTE))
			return "getPenteGame";
		else if (this.equals(GameType.MANANTIALES))
			return "getManantialesGame";
		else
			return "getGameByType";
	}

	public String getNamedQueryById() {
		if (this.equals(GameType.PENTE))
			return "getPenteGameById";
		else if (this.equals(GameType.MANANTIALES))
			return "getManantialesGameById";
		else
			return "getGameById";
	}
	
	public String getNamedQueryByTypeAndPlayer(){
		if (this.equals(GameType.PENTE))
			return "getPenteGameByTypeAndPlayer";
		else if (this.equals(GameType.MANANTIALES))
			return "getManantialesGameByTypeAndPlayer";
		else
			return "getGameByTypeAndPlayer";
	}

	public String getNamedMoveQuery() {
		if (this.equals(GameType.PENTE))
			return "getPenteMoves";
		else if (this.equals(GameType.MANANTIALES))
			return "getManantialesMoves";				
		else
			return "getMoves";
	}
	
	public ArrayList<Color> getColors () {
		ArrayList<Color> ret = new ArrayList<Color>();
		
		if (this.equals(MANANTIALES)) {
			ret.add(Color.YELLOW);
			ret.add(Color.BLUE);
			ret.add(Color.RED);
			ret.add(Color.BLACK);			
		} else if (this.equals(PENTE)) {
			for (Color c : Color.values()) {
				if (c.equals(Color.UNKNOWN))
					continue;
				if (c.equals (Color.BLACK))
					continue;
				ret.add(c);
			}
		}
		
		return ret;
	}

	/**
	 * Gets the ruleBase for a given game type. If the ruleBase has not
	 * previously been created for this game type it is created.
	 */
	public RuleBase getRuleBase() {

		/* Check that rule set has not already been created */
		if (ruleBase != null) {
			return ruleBase;
		}

		try {

			logger.fine("Initializing rule set for type " + this);

			/* Initialize the rules based on the type of game */
			PackageBuilder builder = new PackageBuilder();
			InputStreamReader reader = null;

			switch (this) {
				case PENTE:
					reader = new InputStreamReader(this.getClass()
							.getResourceAsStream("/mx/ecosur/multigame/gente.drl"));
					builder.addPackageFromDrl(reader);
					break;
				case MANANTIALES:
					reader = new InputStreamReader(this.getClass()
							.getResourceAsStream("/mx/ecosur/multigame/manantiales.drl"));
					builder.addPackageFromDrl(reader);
					break;					
				default:
					break;
			}

			if (reader != null)
				reader.close();

			/* Create the ruleBase */
			ruleBase = RuleBaseFactory.newRuleBase();
			ruleBase = RuleBaseFactory.newRuleBase();
			ruleBase.addPackage(builder.getPackage());

			logger.fine("Rule set for type " + this + " added to rulesets.");
			return ruleBase;

		} catch (DroolsParserException e) {
			e.printStackTrace();
			throw new RuntimeException (e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.Type#getEnum()
	 */
	public Enum getEnum() {
		return this;
	}
}
