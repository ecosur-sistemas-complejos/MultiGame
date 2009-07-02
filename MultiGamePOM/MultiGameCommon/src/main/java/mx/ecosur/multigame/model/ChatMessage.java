/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.model;

import mx.ecosur.multigame.model.implementation.ChatMessageImpl;
import mx.ecosur.multigame.model.implementation.GamePlayerImpl;

public class ChatMessage implements Model {
	
	private ChatMessageImpl chatMessageImpl;

	public ChatMessage (ChatMessageImpl chatMessageImpl) {
		this.chatMessageImpl = chatMessageImpl;
	}

	/**
	 * @return
	 */
	public GamePlayer getSender() {
		GamePlayerImpl sender =  chatMessageImpl.getSender();
		return new GamePlayer (sender);
	}

	/**
	 * @param find
	 */
	public void setSender(GamePlayer agent) {		
		chatMessageImpl.setAgent (agent.getImplementation());
	}

}
