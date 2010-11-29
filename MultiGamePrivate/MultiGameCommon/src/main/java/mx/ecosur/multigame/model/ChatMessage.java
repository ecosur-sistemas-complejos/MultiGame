/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
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
import mx.ecosur.multigame.model.implementation.Implementation;

public class ChatMessage implements Model {

    private static final long serialVersionUID = -5474115636921841270L;

    private ChatMessageImpl chatMessageImpl;

    public ChatMessage () {
        super();
    }

    public ChatMessage (ChatMessageImpl chatMessageImpl) {
        this.chatMessageImpl = chatMessageImpl;
    }

    /**
    * @return
    */
    public GamePlayer getSender() {
        GamePlayerImpl sender =  chatMessageImpl.getSender();
        if (sender == null)
            return null;
        else
            return new GamePlayer (sender);
    }

    /**
    * @param find
    */
    public void setSender(GamePlayer agent) {
        chatMessageImpl.setSender (agent.getImplementation());
    }

    /**
    * @return the chatMessageImpl
    */
    public ChatMessageImpl getImplementation() {
        return chatMessageImpl;
    }

    /* (non-Javadoc)
    * @see mx.ecosur.multigame.model.Model#setImplementation(mx.ecosur.multigame.model.implementation.Implementation)
    */
    public void setImplementation(Implementation impl) {
        this.chatMessageImpl = (ChatMessageImpl) impl;
    }

}
