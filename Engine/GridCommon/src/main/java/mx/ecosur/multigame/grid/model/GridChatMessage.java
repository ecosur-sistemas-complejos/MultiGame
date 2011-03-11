/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * A chat message sent across the system.
 * 
 * @author max@alwaysunny.com
 */

package mx.ecosur.multigame.grid.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import mx.ecosur.multigame.model.interfaces.ChatMessage;
import mx.ecosur.multigame.model.interfaces.GamePlayer;

@Entity
public class GridChatMessage implements ChatMessage {

    private static final long serialVersionUID = 444999377280040070L;

    /* TODO: Chat messages should be related to moves */

    private int id;

    private GridPlayer sender;

    private long dateSent;

    private String body;

    @Id @GeneratedValue
    public int getId() {
            return id;
    }

    public void setId(int id) {
            this.id = id;
    }

    public GamePlayer getSender() {
        return sender;
    }

    public void setSender(GridPlayer sender) {
        this.sender = sender;
    }

    @Temporal(TemporalType.DATE)
    public Date getDateSent() {
        return new Date(dateSent);
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent.getTime();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.model.interfaces.ChatMessage#setAgent(mx.ecosur.multigame.model.interfaces.Agent)
     */
    public void setSender(GamePlayer agent) {
        this.sender = (GridPlayer) agent;
    }
}
