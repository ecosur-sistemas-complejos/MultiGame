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
			String gameType = msg.getStringProperty("GAME_TYPE"); 
			if (gameType != null && gameType.equals (GameType.PENTE)) {
				int gameId = msg.getIntProperty("GAME_ID");
				GameEvent gameEvent = GameEvent.valueOf(msg.getStringProperty(
				"GAME_EVENT"));
				switch (gameEvent) {
					case BEGIN:
						logStart(gameId);
						// Fall through
					case PLAYER_CHANGE:
						handleEvent (gameId);
						break;
					default:
						throw new RuntimeException ("JMSPlayer encounted unhandled" +
								"game event type! (" + gameEvent.toString() + ")");
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
	
	public void logStart (int gameId) {
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
	 * @param gameId 
	 * @throws InvalidMoveException 
	 * 
	 */
	private void handleEvent (int gameId) throws InvalidMoveException {
		List <GamePlayer> players = sharedBoard.getPlayers(gameId);
		for (GamePlayer p : players) {
			if (p instanceof StrategyPlayer) {
				StrategyPlayer player = (StrategyPlayer) p;
				if (player.isTurn()) {
					PenteMove qualifier = player.determineNextMove();
					Move move = sharedBoard.validateMove(qualifier);
					sharedBoard.move(move);
				}
			}	
		}
	}

}
