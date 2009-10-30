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
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Move;
import mx.ecosur.multigame.model.implementation.GameImpl;

@MessageDriven(mappedName = "MultiGame")
public class AgentListener implements MessageListener {
	
	@EJB
	private SharedBoardRemote sharedBoard;

	private static final Logger logger = Logger.getLogger(
			AgentListener.class.getCanonicalName());

	private static final long serialVersionUID = -312450142866686545L;

	public void onMessage(Message message) {			
		try {
			GameEvent gameEvent = GameEvent.valueOf(message.getStringProperty(
				"GAME_EVENT"));
			ObjectMessage msg = (ObjectMessage) message;

			if (gameEvent.equals(GameEvent.PLAYER_CHANGE)) {
				GameImpl game = (GameImpl) msg.getObject();
				List<GamePlayer> players = game.listPlayers();
				for (GamePlayer p : players) {
					if (p instanceof Agent) {
						Agent agent = (Agent) p;
						if (game.getState() == GameState.PLAY) {
							try {					
								Move move = agent.determineNextMove(new Game (game));
                                if (move.getImplementation() != null)
								    sharedBoard.doMove(new Game (game), move);								                                    
                                message.acknowledge();
							} catch (InvalidMoveException e) {
								logger.log(Level.SEVERE, "Invalid move suggested " +
										"by agent!");
								e.printStackTrace();
							}
						}
					}
				}
			}
			
		} catch (JMSException e) {
			e.printStackTrace();
		} 
	}
}
