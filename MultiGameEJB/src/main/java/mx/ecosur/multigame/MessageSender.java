package mx.ecosur.multigame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Move;

public class MessageSender {

	private static Logger logger = Logger.getLogger(MessageSender.class
			.getCanonicalName());

	// TODO: Connection factory properties - should be moved out to .properties
	// file
	private static String CONNECTION_FACTORY_JNDI_NAME = "jms/TopicConnectionFactory";
	private static String TOPIC_JNDI_NAME = "CHECKERS";

	/* Message keys */
	private static String KEY_GAME_ID = "GAME_ID";
	private static String KEY_GAME_EVENT = "GAME_EVENT";
	private static String KEY_MESSAGE_ID = "MESSAGE_ID";

	private ConnectionFactory connectionFactory;
	private Topic topic;
	
	/*
	 * Message counter to allow clients to order and queue messages Keys
	 * correspond to gameId's and the values the id of the last message sent to
	 * that game where the ids of messages within a game start at 1 and are
	 * incremented by 1 for each message enabling the clients to order messages
	 * and check for missing deliveries.
	 * 
	 * A Hashtable is used since it is synchronized which should handle concurrency
	 */
	private static Hashtable<Integer,Integer> lastMessageIds = new Hashtable<Integer, Integer>();

	/**
	 * Default constructor initializes connection factory and topic
	 */
	public MessageSender() {
		super();
		InitialContext ic;
		try {
			ic = new InitialContext();
			connectionFactory = (ConnectionFactory) ic
					.lookup(CONNECTION_FACTORY_JNDI_NAME);
			topic = (Topic) ic.lookup(TOPIC_JNDI_NAME);
		} catch (NamingException e) {
			logger
					.severe("Not able to get JMS connection and topic from connection factory "
							+ CONNECTION_FACTORY_JNDI_NAME
							+ " and topic "
							+ TOPIC_JNDI_NAME);
			e.printStackTrace();
		}

	}

	private int getNextId(int gameId) {
		int messageId = 1;
		if (lastMessageIds.containsKey(gameId)){
			messageId = lastMessageIds.get(gameId) + 1;
		}
		lastMessageIds.put(gameId, messageId);
		return messageId;
	}

	private void sendMessage(int gameId, GameEvent gameEvent, Serializable body) {
		try {
			Connection connection = connectionFactory.createConnection();
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(topic);
			ObjectMessage message = session.createObjectMessage();
			message.setIntProperty(KEY_GAME_ID, gameId);
			message.setStringProperty(KEY_GAME_EVENT, gameEvent.toString());
			message.setIntProperty(KEY_MESSAGE_ID, getNextId(gameId));
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

	/**
	 * Sends GameEvent.BEGIN message with no data
	 * 
	 * @param game
	 */
	public void sendStartGame(Game game) {
		sendMessage(game.getId(), GameEvent.BEGIN, null);
	}

	/**
	 * Sends the GameEvent.PLAYER_CHANGE message with the current list of
	 * players for the specified game.
	 * 
	 * @param game
	 * @param players
	 */
	public void sendPlayerChange(Game game) {
		// must convert list to array list since list is not serialized
		List<GamePlayer> players = game.getPlayers();
		ArrayList<GamePlayer> playersSerial = new ArrayList<GamePlayer>(players);
		sendMessage(game.getId(), GameEvent.PLAYER_CHANGE, playersSerial);
	}

	/**
	 * Sends the GameEvent.MOVE_COMPLETE message with the move completed.
	 * 
	 * @param move
	 */
	public void sendMoveComplete(Move move) {
		sendMessage(move.getPlayer().getGame().getId(),
				GameEvent.MOVE_COMPLETE, move);
	}

	/**
	 * Sends the GameEvent.QUALIFY_MOVE message with the move to be qualified.
	 * 
	 * @param move
	 */
	public void sendQualifyMove(Move move) {
		sendMessage(move.getPlayer().getGame().getId(), GameEvent.QUALIFY_MOVE,
				move);
	}

	/**
	 * Sends GameEvent.END message with no data
	 * 
	 * @param game
	 */
	public void sendEndGame(Game game) {
		sendMessage(game.getId(), GameEvent.END, game);
	}

}
