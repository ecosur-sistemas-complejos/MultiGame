/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * A JMSPLayer is a rule based Pente game player that plays with a particular 
 * strategy, and activates based upon JMS message flow.  
 * 
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ejb.jms;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;

import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.enums.GameState;

import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.model.Agent;
import mx.ecosur.multigame.model.Move;

@MessageDriven(mappedName = "MultiGame")
public class AgentListener implements MessageListener {
	
	@EJB
	private SharedBoardRemote sharedBoard;

	private static Logger logger = Logger.getLogger(
			AgentListener.class.getCanonicalName());

	private static final long serialVersionUID = -312450142866686545L;

	public void onMessage(Message msg) {			
		try {
			GameEvent gameEvent = GameEvent.valueOf(msg.getStringProperty(
				"GAME_EVENT"));
			
			/* The listener should only be activated when the Game Begins,
			 * or a Move has been completed.
			 */
			switch (gameEvent) {
				case PLAYER_CHANGE:
					handleEvent ((ObjectMessage) msg);
					break;
				default:
					break;
			}
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/**
	 * 
	 * 
	 * @param msg 
	 * @throws InvalidMoveException 
	 * @throws JMSException 
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void handleEvent (ObjectMessage message) throws JMSException 
	{
		List<Agent> players = (List<Agent>) message.getObject();
		for (Agent p : players) {
			if (p instanceof Agent) {
				Agent agent = (Agent) p;
				if (agent.isTurn() && agent.getGame().getState() != GameState.END) {
					/* Simple 50ms sleep */
					try {
						Thread.sleep(250);					
						Move move = agent.nextMove();		
						logger.info("Agent moving: " + move);
						sharedBoard.move(move);				
						message.acknowledge();
					} catch (InterruptedException e) {
						handleEvent (message);
						e.printStackTrace();
					} catch (InvalidMoveException e) {
						logger.log(Level.SEVERE, "Invalid move suggested " +
								"by agent!");
						e.printStackTrace();
					}
					
				}
			}	
		}
	}
}
