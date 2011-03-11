/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 *
 * DummyMessageSender 
 *
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.grid;

import java.io.Serializable;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.enums.GameEvent;

/**
 *  DummyMessageSender is used by Agents to determine possible moves against
 *  a ruleset that requires a message sender object.
 */
public class DummyMessageSender extends MessageSender {

    public DummyMessageSender () {
        super ();
    }

    @Override
    public void sendMessage(int gameId, GameEvent gameEvent, Serializable body) {
       // do nothing
    }
}
