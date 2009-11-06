/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
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
package mx.ecosur.multigame.impl;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.enums.GameEvent;

import javax.naming.InitialContext;
import java.io.Serializable;

/**
 *  DummyMessageSender is used by Agents to determine possible moves against
 *  a ruleset that requires a message sender object.
 */
public class DummyMessageSender extends MessageSender {

    public DummyMessageSender () {
        //
    }

    @Override
    public void sendMessage(int gameId, GameEvent gameEvent, Serializable body) {
       // do nothing
    }
}
