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

package mx.ecosur.multigame.ejb.jms.pente;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import mx.ecosur.multigame.GameEvent;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;
import mx.ecosur.multigame.ejb.entity.pente.PenteMove;
import mx.ecosur.multigame.ejb.entity.pente.StrategyPlayer;
import mx.ecosur.multigame.exception.InvalidMoveException;

@MessageDriven(mappedName = "CHECKERS")
public class StrategyPlayerListener implements MessageListener {
	
	@EJB
	private SharedBoardRemote sharedBoard;

	private static Logger logger = Logger.getLogger(
			StrategyPlayerListener.class.getCanonicalName());

	private static final long serialVersionUID = -312450142866686545L;

	public void onMessage(Message msg) {			
		try {
			if (msg.getStringProperty("GAME_TYPE") != null && 
					GameType.valueOf(msg.getStringProperty("GAME_TYPE")).equals (
							GameType.PENTE)) 
			{
				int gameId = msg.getIntProperty("GAME_ID");
				GameEvent gameEvent = GameEvent.valueOf(msg.getStringProperty(
				"GAME_EVENT"));
				
				/* The listener should only be activated when the Game Begins,
				 * or a Move has been completed.
				 */
				switch (gameEvent) {
					case BEGIN:
						logStart(gameId);
						break;
					case PLAYER_CHANGE:
						handleEvent ((ObjectMessage) msg);
						break;
					default:
						break;
				}
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void logStart (int gameId) {
		List <GamePlayer> players = sharedBoard.getPlayers(gameId);
		for (GamePlayer p : players) {
			if (p instanceof StrategyPlayer) {
				logger.fine("All players have joined.  Beginning Strategy game.");
				break;
			}
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
	private void handleEvent (ObjectMessage message) throws InvalidMoveException, 
		JMSException 
	{
		List<GamePlayer> players = (List<GamePlayer>) message.getObject();
		for (GamePlayer p : players) {
			if (p instanceof StrategyPlayer) {
				StrategyPlayer player = (StrategyPlayer) p;
				if (player.isTurn()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
					PenteMove qualifier = player.determineNextMove();
					Move move = sharedBoard.validateMove(qualifier);
					logger.info("Robot making move: " + move);
					sharedBoard.move(move);
				}
			}	
		}
	}
}
