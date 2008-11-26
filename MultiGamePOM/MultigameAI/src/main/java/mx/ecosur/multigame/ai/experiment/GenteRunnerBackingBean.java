/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ai.experiment;

import java.io.Serializable;

import javax.faces.event.ActionEvent;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.myfaces.trinidad.component.core.nav.CoreCommandButton;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Player;
import mx.ecosur.multigame.ejb.entity.pente.PenteGame;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.pente.PenteStrategy;

/**
 * The ExperimentRunnerBackingBean is used for executing a set of iterations
 * of a specified Strategy set against the game of Gente.  
 * 
 * @author "Andrew Waterman" <awaterma@ecosur.mx>
 */
public class GenteRunnerBackingBean implements Serializable {
	
	private static final long serialVersionUID = 7849563672883947189L;
	private int iterations;
	private GenteRobotBoundedRangeModel executions;
	private String strategy;
	private RegistrarRemote registrar;
	private SharedBoardRemote sharedBoard;
	private boolean executing;
	
	public GenteRunnerBackingBean () {
		super();
		iterations = 0;
		executions = new GenteRobotBoundedRangeModel();
			/* Default Strategy */
		strategy = PenteStrategy.SIMPLE.name();
	}
	
	public int getIterations() {
		return iterations;
	}
	
	public String getStrategy() {
		return strategy;
	}
	
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
	
	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
	
	public GenteRobotBoundedRangeModel getExecutions() {
		return executions;
	}
	
	public void setExecutions(GenteRobotBoundedRangeModel executions) {
		this.executions = executions;
	}
	
	public boolean isExecuting() {
		return executing;
	}

	public void setExecuting(boolean executing) {
		this.executing = executing;
	}

	public void execute () throws NamingException {
		executing = true;
		executions = new GenteRobotBoundedRangeModel();
		
		if (registrar == null) {
			InitialContext ctxt = new InitialContext();
			registrar = (RegistrarRemote) ctxt.lookup(
					"mx.ecosur.multigame.ejb.RegistrarRemote");			
		}
		
		if (sharedBoard ==null) {
			InitialContext ctxt = new InitialContext();
			sharedBoard = (SharedBoardRemote) ctxt.lookup(
					"mx.ecosur.multigame.ejb.SharedBoardRemote");
		}
	
		try {
			/* Player "iterations" games with these users */
			for (int i = 0; i < iterations; i++) {
				PenteGame game = createGame ();
				int gameId = game.getId();
				game.setState(GameState.BEGIN);
				/* Loop until game complete */
				while (!game.getState().equals(GameState.END) && (executing)) {
					game = (PenteGame) sharedBoard.getGame(gameId);
					Thread.sleep (100);
				}
				
				if (!executing)
					break;
				this.executions.updateCurrent();
			}
		} catch (InvalidRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void cancel () {
		executing = false;
	}
	
	public void swapStatus (ActionEvent event) {
		CoreCommandButton button = (CoreCommandButton) event.getComponent();
		button.setDisabled(! button.isDisabled());
	}

	
	private PenteGame createGame () throws InvalidRegistrationException {
		PenteGame game = (PenteGame) registrar.createGame(GameType.PENTE);
		Player[] registrants = {
				new Player ("Robot1-" + this.strategy),
				new Player ("Robot2-" + strategy),
				new Player ("Robot3-" + strategy),
				new Player ("Robot4-" + strategy)};
		PenteStrategy penteStrategy = PenteStrategy.valueOf(PenteStrategy.class,
				strategy);
		
		/* Register 4 strategy based users for the initial game */
		for (int i = 0; i < 4; i ++) {
			registrar.registerRobot(game, registrants[i], Color.UNKNOWN, 
					penteStrategy);
		}
		
		return game;
	}
}
