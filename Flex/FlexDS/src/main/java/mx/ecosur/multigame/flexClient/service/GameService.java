/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

package mx.ecosur.multigame.flexClient.service;

import java.security.Principal;
import java.util.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.exception.InvalidSuggestionException;
import mx.ecosur.multigame.flexClient.exception.GameException;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.*;
import mx.ecosur.multigame.impl.entity.manantiales.PuzzleSuggestion;
import mx.ecosur.multigame.impl.entity.manantiales.SimpleAgent;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGame;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.grid.entity.GameGrid;
import mx.ecosur.multigame.grid.entity.GridGame;
import mx.ecosur.multigame.grid.entity.GridMove;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.impl.entity.gente.GenteGame;
import mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.enums.gente.*;


import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import mx.ecosur.multigame.model.interfaces.*;

public class GameService {

    private SharedBoardRemote getSharedBoard() {
        FlexSession session = FlexContext.getFlexSession();
        SharedBoardRemote sharedBoard = (SharedBoardRemote) session.getAttribute("sharedBoard");
        if (sharedBoard == null) {
            try {
                InitialContext ic = new InitialContext();
                sharedBoard = (SharedBoardRemote) ic.lookup(SharedBoardRemote.class.getName());
                session.setAttribute("sharedBoard", sharedBoard);
            } catch (NamingException e) {
                e.printStackTrace();
                throw new GameException(e);
            }
        }
        return sharedBoard;
}

    private RegistrarRemote getRegistrar() {
        FlexSession session = FlexContext.getFlexSession();
        RegistrarRemote registrar = (RegistrarRemote) session.getAttribute("registrar");
        if (registrar == null) {
            try {
                InitialContext ic = new InitialContext();
                registrar = (RegistrarRemote) ic.lookup(RegistrarRemote.class.getName());
                session.setAttribute("registrar", registrar);
            } catch (NamingException e) {
                e.printStackTrace();
                throw new GameException(e);
            }
        }
        return registrar;
    }

    public GridRegistrant login (String name) {

        GridRegistrant gr = new GridRegistrant (name);
        RegistrarRemote registrar = this.getRegistrar();
        Registrant registrant = registrar.register(gr);
        return (GridRegistrant) registrant;
    }

    public GridRegistrant registerPrincipal () {  
        FlexSession flexSession = FlexContext.getFlexSession();
        Principal user = flexSession.getUserPrincipal();
        return login (user.getName());
    }

    public ServiceGameEvent startNewGame(GridRegistrant player, Color preferedColor, String gameTypeStr) {
        return startNewGame (player, preferedColor, gameTypeStr, null);
    }

    public ServiceGameEvent startNewGame(GridRegistrant player, Color preferedColor, String gameTypeStr, String mode)
    {
        if (player == null)
            player = registerPrincipal();
        try {
            RegistrarRemote registrar = getRegistrar();
            GridGame game = null;
            GameType type = GameType.valueOf(gameTypeStr.toUpperCase());
            
            switch (type) {
                case GENTE:
                    game = new GenteGame();
                    break;
                case MANANTIALES:
                    if (mode == null)
                        game = new ManantialesGame();
                    else
                        game = new ManantialesGame(Mode.valueOf(mode.toUpperCase()));
                    break;
                case PASALE:
                    game = new PasaleGame();
            }

            game = (GridGame) registrar.registerPlayer(game, player);

            for (GamePlayer impl : game.listPlayers()) {
                if (impl.getName().equals(player.getName())) {
                    return new ServiceGameEvent (game, (GridPlayer) impl);
                }
            }

        } catch (InvalidRegistrationException e) {
            e.printStackTrace();
            throw new GameException(e);
        }

        throw new RuntimeException ("Issue registering player!  Please check logs for details.");
    }

    public ServiceGameEvent joinPendingGame (GridGame game, GridRegistrant registrant,
                    Color preferredColor)
    {
        ServiceGameEvent ret = null;
        RegistrarRemote registrar = getRegistrar();
        try {
            Game model = registrar.registerPlayer (game, registrant);
            for (GamePlayer gp : model.listPlayers()) {
                GridPlayer player = (GridPlayer) gp;
                if (player.getName().equals(registrant.getName())) {
                    ret = new ServiceGameEvent ((GridGame) model, player);
                    break;
                }
            }

        } catch (InvalidRegistrationException e) {
                e.printStackTrace();
                throw new GameException (e);
        }

        return ret;
    }

    public boolean quitGame (GridGame game, GridPlayer player) {
        boolean ret = false;
        try {
            RegistrarRemote registrar = getRegistrar();
            registrar.unregister (game, player);
            ret = true;
        } catch (InvalidRegistrationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ret;
    }

    /*
     * Starts a new game with a given player and the number of selected AI robots.
     */
    public ServiceGameEvent startNewGameWithAI (GridRegistrant registrant, Color preferredColor,
        String gameTypeStr, String [] strategies)
    {
        if (registrant == null)
            registrant = registerPrincipal();        

        ServiceGameEvent ret = null;
        try {
            RegistrarRemote registrar = getRegistrar();
            Game model = null;
            GameType gameType = GameType.valueOf(gameTypeStr);
            if (gameType.equals(GameType.GENTE)) {
                GridGame game = new GenteGame ();
                model = registrar.registerPlayer(game, registrant);
                for (int i = 0; i < strategies.length; i++) {
                    if (strategies [ i ].equals("HUMAN"))
                        continue;
                    GenteStrategy strategy = GenteStrategy.valueOf(
                        strategies [ i ]);
                    GridRegistrant robot = new GridRegistrant (gameType + "-" + strategy.name() + "-" + (i + 1));
                    GenteStrategyAgent agent = new GenteStrategyAgent (robot, Color.UNKNOWN, strategy);
                    model = registrar.registerAgent (model, agent);
                }
            }

            if (model != null) {
                for (GamePlayer player : model.listPlayers()) {
                    GridPlayer gp = (GridPlayer) player;
                    if (gp.getName().equals(registrant.getName())) {
                        ret = new ServiceGameEvent ((GridGame) model, gp);
                        break;
                    }
                }
            }

        } catch (InvalidRegistrationException e) {
            throw new GameException (e);
        }

        return ret;
    }

    public ServiceGameEvent startNewGameWithAI (GridRegistrant registrant, Color preferredColor,
        String gameTypeStr, String mode, String [] strategies)
    {
        ServiceGameEvent ret = null;

        try {
            if (registrant == null)
                registrant = registerPrincipal();

            RegistrarRemote registrar = getRegistrar();
            Game game = null;
            GameType gameType = GameType.valueOf(gameTypeStr);

            if (gameType.equals(GameType.MANANTIALES)) {
                ManantialesGame mg = new ManantialesGame ();
                mg.setMode(Mode.valueOf(mode));
                game = registrar.registerPlayer(mg, registrant);
                for (int i = 0; i < strategies.length; i++) {
                    AgentType strategy = AgentType.valueOf(strategies [ i ]);
                    GridRegistrant robot = new GridRegistrant (gameType + "-" + strategy.name() + "-" + (i + 1));
                    SimpleAgent agent = new SimpleAgent(robot, Color.UNKNOWN, strategy);
                    game = registrar.registerAgent (game, agent);
                }
            }

            if (game != null) {
                for (GamePlayer player : game.listPlayers()) {
                    GridPlayer gp = (GridPlayer) player;
                    if (gp.getName().equals(registrant.getName())) {
                        ret = new ServiceGameEvent ((GridGame) game, gp);
                        break;
                    }
                }
            }

        } catch (InvalidRegistrationException e) {
            throw new GameException (e);
        }

        return ret;
    }


    public List<GridGame> getUnfinishedGames(GridRegistrant player){
        if (player == null)
            player = registerPrincipal();

        RegistrarRemote registrar = getRegistrar();
        Collection<Game> games = registrar.getUnfinishedGames(player);
        List<GridGame> ret = new ArrayList<GridGame>();
        for (Game game : games)
            ret.add((GridGame) game);
        return ret;
    }

    public List<GridGame> getPendingGames(GridRegistrant player){
        if (player == null)
            player = registerPrincipal();
                
        RegistrarRemote registrar = getRegistrar();
        Collection<Game> games = registrar.getPendingGames(player);
        List<GridGame> ret = new ArrayList<GridGame>();
        for (Game game : games)
           ret.add((GridGame) game);
        return ret;
    }

    public GridGame getGame(int gameId) {
        SharedBoardRemote sharedBoard = getSharedBoard();
        Game game = sharedBoard.getGame(gameId);
        return (GridGame) game;
    }

    public GameGrid getGameGrid(int gameId) {
        SharedBoardRemote sharedBoard = getSharedBoard();
        Game game = sharedBoard.getGame(gameId);
        GridGame gridGame = (GridGame) game;
        return gridGame.getGrid();
    }

    public GridGame getPlayers(int gameId) {
        SharedBoardRemote sharedBoard = getSharedBoard();
        Game game = sharedBoard.getGame(gameId);
        return (GridGame) game;
    }

    public Move doMove(GridGame game, GridMove move) {
        SharedBoardRemote sharedBoard = getSharedBoard();
        try {
            return sharedBoard.doMove(game, move);
        } catch (InvalidMoveException e) {
            e.printStackTrace();
            throw new GameException(e);
        }
    }

    public Set<GridMove> getMoves(int gameId) {
        SharedBoardRemote sharedBoard = getSharedBoard();
        Game game = sharedBoard.getGame(gameId);
        GridGame impl = (GridGame) game;
        return impl.getMoves();
    }

    public Set<GridMove> getMoves (int gameId, Mode mode) {
        SharedBoardRemote sharedBoard = getSharedBoard();
        Game game = sharedBoard.getGame(gameId);
        ManantialesGame mg = (ManantialesGame) game;
        mg.setMode(mode);
        return mg.getMoves();
    }

    public GridMove updateMove(GridMove move) {
        SharedBoardRemote sharedBoard = getSharedBoard();
        move = (GridMove) sharedBoard.updateMove(move);
        return move;
    }

    /* Manantiales Specific Methods */

    public PuzzleSuggestion makeSuggestion (GridGame game, PuzzleSuggestion suggestion) throws
            InvalidSuggestionException
    {
        SharedBoardRemote sharedBoard = getSharedBoard();     
        Suggestion ret = sharedBoard.makeSuggestion (game, suggestion);
        return (PuzzleSuggestion) ret;
    }

    public Set<PuzzleSuggestion> getSuggestions (int gameId, SuggestionStatus status) {
        LinkedHashSet<PuzzleSuggestion> ret = new LinkedHashSet<PuzzleSuggestion>();
        Game game = getSharedBoard().getGame(gameId);
        ManantialesGame mg = (ManantialesGame) game;
        for (PuzzleSuggestion sug : mg.getSuggestions()) {
            if (sug.getStatus().equals(status))
                ret.add(sug);
        }

        return ret;
    }
}
