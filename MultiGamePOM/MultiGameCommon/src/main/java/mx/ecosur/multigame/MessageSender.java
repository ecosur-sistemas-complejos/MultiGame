/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Sends JMS messages on the behalf of a client.  For our prototype, this
 * client is usually from within the rules definition (drl) file.
 * 
 * @author max@alwayssunny.com
 */

package mx.ecosur.multigame;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.model.Condition;
import mx.ecosur.multigame.model.implementation.*;

public class MessageSender {

	private static Logger logger = Logger.getLogger(MessageSender.class
			.getCanonicalName());

	private static final String CONNECTION_FACTORY_JNDI_NAME = "jms/TopicConnectionFactory";
	private static final String TOPIC_JNDI_NAME = "MultiGame";

	@Resource(mappedName = CONNECTION_FACTORY_JNDI_NAME)
	private ConnectionFactory connectionFactory;
	
	@Resource (mappedName = TOPIC_JNDI_NAME)
	private Topic topic;
	
	private static Map<Integer, Long> msgIdCount = new HashMap<Integer, Long>();

	/**
	 * Default constructor initializes connection factory and topic
	 */
	public MessageSender() {
		super();
		if (connectionFactory == null || topic == null) 
		{
			InitialContext ic;
			try {
				ic = new InitialContext();
				if (connectionFactory == null)
					connectionFactory = (ConnectionFactory) ic
						.lookup(CONNECTION_FACTORY_JNDI_NAME);
				if (topic == null)
					topic = (Topic) ic.lookup(TOPIC_JNDI_NAME);
			} catch (Exception e) {
				logger
						.severe("Unable to get JMS connection and topic from " +
								"connection factory " + CONNECTION_FACTORY_JNDI_NAME
								+ " and topic " + TOPIC_JNDI_NAME);
				e.printStackTrace();
			}
		}

	}
	
	public MessageSender (Context context) {
		try {
			if (connectionFactory == null)
				connectionFactory = (ConnectionFactory) context
					.lookup(CONNECTION_FACTORY_JNDI_NAME);
			if (topic == null)
				topic = (Topic) context.lookup(TOPIC_JNDI_NAME);
		} catch (NamingException e) {
			logger
					.severe("Not able to get JMS connection and topic from " +
							"connection factory " + CONNECTION_FACTORY_JNDI_NAME
							+ " and topic " + TOPIC_JNDI_NAME);
			e.printStackTrace();
		}
	}

	public void sendMessage(int gameId, GameEvent gameEvent, Serializable body) 
	{
		try {
			Connection connection = connectionFactory.createConnection();
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(topic);
			ObjectMessage message = session.createObjectMessage();
			message.setIntProperty("GAME_ID", gameId);
			message.setStringProperty("GAME_EVENT", gameEvent.toString());
			message.setLongProperty("MESSAGE_ID", getNextMessageId(gameId));				
			if (body != null) {
				message.setObject(body);
			}
			producer.send(message);
			session.close();
			connection.close();
		} catch (JMSException e) {
			logger.severe("Not able to send message");
			e.printStackTrace();
		}
	}
	
	private long getNextMessageId(int gameId){
		if (msgIdCount.containsKey(gameId)){
			msgIdCount.put(gameId, msgIdCount.get(gameId) + 1);
		} else {
			msgIdCount.put(gameId, (long) 1);
		}
		return msgIdCount.get(gameId);
	}

	/**
	 * Sends GameEvent.BEGIN message with no data
	 * 
	 * @param game
	 */
	public void sendStartGame(GameImpl game) {
		sendMessage(game.getId(), GameEvent.BEGIN, game);
	}

	/**
	 * Sends the GameEvent.PLAYER_CHANGE message with the current list of
	 * players for the specified game.
	 * 
	 * @param game
	 */
	public void sendPlayerChange(GameImpl game) {
		sendMessage(game.getId(), GameEvent.PLAYER_CHANGE, game);
	}

	/**
	 * Sends the GameEvent.MOVE_COMPLETE message with the move completed.
	 * 
	 * @param move
	 */
	public void sendMoveComplete(GameImpl game, MoveImpl move) {
		sendMessage(game.getId(), GameEvent.MOVE_COMPLETE, move);
	}
	
	/**
	 * Sends the GameEvent.CONDITION_RAISED message with the raised condition.
	 * 
	 * @param move
	 */
	public void sendConditionRaised (GameImpl game, MoveImpl move, ConditionImpl condition) {
		sendMessage(game.getId(), GameEvent.CONDITION_RAISED, condition);		
	}
	
	/**
	 * Sends  the GameEvent.CHECK_CONSTRAINT_RESOLVED message with the 
	 * resolved condition.
	 */
	public void sendConditionResolved (GameImpl game, MoveImpl move, ConditionImpl condition) {
		sendMessage (game.getId(), GameEvent.CONDITION_RESOLVED, condition);
	}
	
	/** 
	 * Sends the GameEvent.CONDITION_TRIGGERED message with the triggered
	 * condition.
	 */
	public void sendConditionTriggered (GameImpl game, MoveImpl move, ConditionImpl condition) {
		sendMessage (game.getId(), GameEvent.CONDITION_TRIGGERED, condition);		
	}
	
	/** 
	 * Sends the GameEvent.STATE_CHANGE message with the game object.
	 */
	public void sendStateChange (GameImpl game) {
		sendMessage(game.getId(), GameEvent.STATE_CHANGE, game);
	}

	/**
	 * Sends GameEvent.END message with the game object.
	 * 
	 * @param game
	 */
	public void sendEndGame(GameImpl game) {
		sendMessage(game.getId(), GameEvent.END, game);
	}

}
