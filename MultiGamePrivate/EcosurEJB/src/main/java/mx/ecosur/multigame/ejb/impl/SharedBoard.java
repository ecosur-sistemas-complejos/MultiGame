/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * The SharedBoardEJB handles operations between players and the shared game
 * board.  The SharedBoardEJB manges game specific events, such as validating a
 * specific move on a game board, making a specific move, and modifying a 
 * previous move.  Clients can also add chat messages to the message stream,
 * increment players turns (soon to be phased into the game rules), and get
 * a list of players for a specific game.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.ejb.impl;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;

import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.enums.MoveStatus;

import mx.ecosur.multigame.exception.InvalidSuggestionException;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGame;
import mx.ecosur.multigame.model.*;
import mx.ecosur.multigame.model.implementation.*;
import mx.ecosur.multigame.MessageSender;
import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.io.ResourceFactory;

@Stateless
@RolesAllowed("admin")
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class SharedBoard implements SharedBoardLocal, SharedBoardRemote {

    private MessageSender messageSender;
        
    @PersistenceContext (unitName="MultiGame")
    EntityManager em;

    public SharedBoard () throws InstantiationException, IllegalAccessException,
            ClassNotFoundException
    {
        super();
        messageSender = new MessageSender();
        messageSender.initialize();
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.ejb.SharedBoardLocal#getGame(int)
     */
    public Game getGame(int gameId) {
        /** TODO:  Inject or make this query static */
        Query query = em.createNamedQuery("getGameById");
        query.setParameter("id", gameId);
        GameImpl impl;
        try {
            impl = (GameImpl) query.getSingleResult();
        } catch (NoResultException e) {
            throw new RuntimeException ("UNABLE TO FIND GAME WITH ID: " + gameId);
        }

        Game game = new Game (impl);
        game.setMessageSender(messageSender);
        return game;
    }

    public void shareGame (GameImpl impl) {
        try {
            em.merge(impl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.ejb.SharedBoardRemote#move(mx.ecosur.multigame.model.Move)
     */
    public Move doMove(Game game, Move move) throws InvalidMoveException {
        /* Refresh a detached GamePlayer in the Move */
        GamePlayer player = move.getPlayer();

        /* Refresh a detached Game in GamePlayer */
        if (!em.contains (game.getImplementation())) {
            GameImpl impl = em.find (game.getImplementation().getClass(),
                game.getId());
            game = new Game (impl);
        }

        if (!em.contains (player.getImplementation())) {
            player = new GamePlayer (em.find(
                player.getImplementation().getClass(), player.getId()));
        }

        move.setPlayer(player);


        /* Refresh any detached GridCells */
        Cell current = move.getCurrent();
        Cell dest = move.getDestination();


        if (current.getImplementation() != null) {
            if (!em.contains (current.getImplementation())) {
                CellImpl impl = (em.find (
                    current.getImplementation().getClass(), current.getId()));
                if (impl != null)
                    current = new Cell (impl);
            }

            move.setCurrent (current);
        }

        if (dest.getImplementation() != null) {
            if (!em.contains (dest.getImplementation())) {
                CellImpl impl = (em.find (
                    dest.getImplementation().getClass(), dest.getId()));
                if (impl != null)
                    dest = new Cell (impl);
            }
            move.setDestination (dest);
        }

        if (!em.contains(move.getImplementation())) {
            em.persist(move.getImplementation());
        }
        else {
            Move test = new Move (em.find (move.getImplementation().getClass(),
                move.getImplementation().getId()));
            if (test.getImplementation() != null)
                move.setImplementation(test.getImplementation());
        }

            /* Execute the move */
        game.setMessageSender(messageSender);
        if (game.getImplementation() instanceof PasaleGame) {
            PasaleGame pg = (PasaleGame) game.getImplementation();
            pg.setKbase(null);
            game.setImplementation(pg);
        }

        move = game.move (move);
        if (move.getStatus().equals(MoveStatus.INVALID))
            throw new InvalidMoveException ("INVALID Move. [" + move.getImplementation().toString() + "]");
        return move;
    }


    public Suggestion makeSuggestion(Game game, Suggestion suggestion) throws InvalidSuggestionException {

        /* Refresh a detached Game */
        if (!em.contains (game.getImplementation())) {
            GameImpl impl = em.find (game.getImplementation().getClass(),
                game.getId());
            game = new Game (impl);
        } else
            em.refresh (game.getImplementation());        

            /* Refresh a detached GamePlayer in the Suggestion */
        GamePlayer player = suggestion.listSuggestor();

        if (!em.contains (player.getImplementation())) {
            player = new GamePlayer (em.find(
                player.getImplementation().getClass(), player.getId()));
            suggestion.attachSuggestor (player.getImplementation());
        }

        /* Refresh any detached GridCells and Move */
        Move move = suggestion.listMove();
        Cell current = move.getCurrent();
        Cell dest = move.getDestination();

        if (current.getImplementation() != null) {
            if (!em.contains (current.getImplementation())) {
                CellImpl impl = (em.find (
                    current.getImplementation().getClass(), current.getId()));
                if (impl != null)
                    current = new Cell (impl);
                else
                    em.persist(current.getImplementation());
            }
        }

        if (dest.getImplementation() != null) {
            if (!em.contains (dest.getImplementation())) {
                CellImpl impl = (em.find (
                    dest.getImplementation().getClass(), dest.getId()));
                if (impl != null)
                    dest = new Cell (impl);
                else
                    em.persist(dest.getImplementation());
            }

        }

        move.setCurrent (current);
        move.setDestination (dest);

            /* Refresh a detached GamePlayer in the Move */
        player = move.getPlayer();

        if (!em.contains (player.getImplementation())) {
            player = new GamePlayer (em.find(
                player.getImplementation().getClass(), player.getId()));
            move.setPlayer(player);
        }

        if (!em.contains(move.getImplementation())) {
            MoveImpl impl = em.find (move.getImplementation().getClass(),
                move.getId());
            if (impl != null) {
                impl.setStatus(move.getStatus());
                move.setImplementation(impl);
            } else {
                em.persist(move.getImplementation());
            }
        }

        suggestion.attachMove(move.getImplementation());

        /* If suggestion has no id, it should be persisted, otherwise reattach */
        if (!em.contains (suggestion.getImplementation())) {
            if (suggestion.getId() == 0)
                em.persist (suggestion.getImplementation());
            else {
                SuggestionImpl impl = em.find (suggestion.getImplementation().getClass(),
                    suggestion.getId());
                if (impl != null) {
                    impl.setStatus(suggestion.getStatus());
                    suggestion.setImplementation(impl);
                }
            }
        }
        
            /* Make the suggestion */
        game.setMessageSender(messageSender);      
        if (game.getImplementation() instanceof PasaleGame) {
            PasaleGame pg = (PasaleGame) game.getImplementation();
            pg.setKbase(null);
            game.setImplementation(pg);
        }
        Suggestion ret = game.suggest(suggestion);

        if (suggestion.getStatus().equals(SuggestionStatus.INVALID))
            throw new InvalidSuggestionException ("INVALID Move.");        

         /* Return the processed suggestion */
        return ret;
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.ejb.SharedBoardRemote#getMoves(int)
     */
    public Collection<Move> getMoves(int gameId) {
        Collection<Move> ret = null;

        Game game = getGame(gameId);
        if (game != null)
            ret = game.getMoves();

        return ret;
    }

    public void addMessage(ChatMessage chatMessage) {
        /* chat message sender may be detatched */
        GamePlayer sender = chatMessage.getSender();

        if (!em.contains(sender.getImplementation())) {
            chatMessage.setSender(new GamePlayer (em.find(sender.getImplementation().getClass(),
                sender.getImplementation().getId())));
        }

        em.merge (chatMessage.getImplementation());
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.ejb.interfaces.SharedBoardInterface#updateMove(mx.ecosur.multigame.model.Move)
     */
    public Move updateMove(Move move) {
        /* Refresh the GamePlayer impl reference and proceed to merge any changes in
         * the move back into the backend
         */
        if (!em.contains(move.getPlayer().getImplementation())) {
            GamePlayer player = new GamePlayer (em.find (
                move.getPlayer().getImplementation().getClass(), move.getPlayer().getImplementation().getId()));
            move.setPlayer(player);
        }

        em.merge(move.getImplementation());
        return move;
    }
}
