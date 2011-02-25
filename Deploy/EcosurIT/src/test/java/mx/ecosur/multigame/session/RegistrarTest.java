/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
* @author awaterma@ecosur.mx
*/
package mx.ecosur.multigame.session;

import static org.junit.Assert.*;

import com.sun.appserv.security.ProgrammaticLogin;

import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.interfaces.GamePlayer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;
import mx.ecosur.multigame.impl.enums.gente.GenteStrategy;
import mx.ecosur.multigame.impl.model.GridGame;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.model.Agent;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.Registrant;

public class RegistrarTest {

    private RegistrarRemote registrar;

    private SharedBoardRemote board;

    private GridGame game;

    @Before
    public void fixtures () throws RemoteException, NamingException, InvalidRegistrationException {
        ProgrammaticLogin login = new ProgrammaticLogin();
        login.login("MultiGame", "test");
        InitialContext ic = new InitialContext();

        registrar = (RegistrarRemote) ic.lookup(
            "mx.ecosur.multigame.ejb.interfaces.RegistrarRemote");

        /* Get the SharedBoard */
        board = (SharedBoardRemote) ic.lookup(
                "mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote");
    }

    @After
    public void tearDown () throws NamingException, RemoteException, InvalidRegistrationException {
        for (GamePlayer player : game.listPlayers()) {
            registrar.unregister(new Game (game), player);    
        }
    }

    @Test
    public void testPlayerRegistration () throws InvalidRegistrationException {
        GridRegistrant player = new GridRegistrant ("Alice");
        game = new GenteGame ();
        Game model = registrar.registerPlayer(new Game(game),
                new Registrant (player));
        for (int i = 0; i < 3; i++) {
            GridRegistrant registrant = new GridRegistrant (
                    "TEST" + "-" + (i + 1));
            model = registrar.registerPlayer (
                    model, new Registrant (registrant));
        }
    }

    @Test
    public void testDuplicatePlayerRegistration () throws InvalidRegistrationException {
        Exception caught = null;
        GridRegistrant player = new GridRegistrant ("Alice");
        game = new GenteGame ();
        Game model = new Game (game);
        model = registrar.registerPlayer(model, new Registrant (player));
        try {
            for (int i = 0; i < 3; i++) {
                GridRegistrant registrant = new GridRegistrant (
                        "TEST");
                model = registrar.registerPlayer (
                        model, new Registrant (registrant));
            }
        } catch (Exception e) {
            caught = e;
        }

        assertNotNull ("Expected exception not caught!", caught);

        /* Register remainder of players in order to close out game */


    }

    @Test
    public void testSimpleRobotRegistration () throws InvalidRegistrationException {
        GridRegistrant player = new GridRegistrant ("Alice");
        game = new GenteGame ();
        Game model = new Game (game);
        model = registrar.registerPlayer(model,
                new Registrant (player));
        for (int i = 0; i < 3; i++) {
            GenteStrategy strategy = GenteStrategy.valueOf(
                    "SIMPLE");
            GridRegistrant robot = new GridRegistrant (
                    strategy.name() + "-" + (i + 1));
            GenteStrategyAgent agent = new GenteStrategyAgent (robot, Color.UNKNOWN, strategy);
            registrar.registerAgent (model, new Agent (agent));
        }
    }

}
