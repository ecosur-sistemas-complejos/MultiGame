package mx.ecosur.multigame.ejb.entity;

/**
 * A chat message sent across the system.
 */
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ChatMessage implements Serializable {
	
	//TODO: Chat messages should be related to moves
	
	/**
	 * Primary key
	 */
	private int id;
	
	/**
	 * The player that sent the message
	 */
	private Player sender;
	
	/**
	 * The time and date that the message was sent
	 */
	private Date dateSent;
	
	/**
	 * The body of the message 
	 */
	private String body;
	
	@Id @GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Player getSender() {
		return sender;
	}

	public void setSender(Player sender) {
		this.sender = sender;
	}

	@Temporal(TemporalType.DATE)
	public Date getDateSent() {
		return dateSent;
	}

	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
