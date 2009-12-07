/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Registration is the process of adding or finding users in the system and
 * associating that user with a current or new game.  Ficha colors are 
 * determined dynamically, by the available colors per game.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.ejb.impl;

import java.util.List;
import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.RegistrarLocal;
import mx.ecosur.multigame.enums.GameState;

import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.model.Agent;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Registrant;
import mx.ecosur.multigame.model.implementation.RegistrantImpl;
import mx.ecosur.multigame.model.implementation.GameImpl;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class Registrar implements RegistrarRemote, RegistrarLocal {

	private MessageSender messageSender;
	
	@PersistenceContext (unitName="MultiGame")
	EntityManager em;

	/**
	 * Default constructor
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Registrar() throws InstantiationException, IllegalAccessException, 
		ClassNotFoundException 
	{
		super();
		messageSender = new MessageSender();
        messageSender.initialize();
	}
	
	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RegistrarInterface#register(java.lang.String)
	 */
    @SuppressWarnings ("unchecked")
	public Registrant register(Registrant registrant) {
		Registrant ret;
		RegistrantImpl impl = registrant.getImplementation();
		
		/* TODO: inject or make this query static */
		Query query = em.createNamedQuery("getRegistrantByName");
		query.setParameter("name", impl.getName());
		List<RegistrantImpl> registrants = query.getResultList();
		if (registrants.size() == 0) { 
			em.persist(impl);
			ret = new Registrant (impl);
		} else {			
			RegistrantImpl reg = (RegistrantImpl) registrants.get(0);
			ret = new Registrant (reg);
		}
		
		return ret;
	}		
	
	/**
	 * Registers a robot with she specified Game object.
	 * 
	 * TODO:  Make this generic.
	 * @throws InvalidRegistrationException 
	 */
	public Game registerPlayer (Game game, Registrant registrant)
		throws InvalidRegistrationException 
	{
        /* Set messaging */
        game.setMessageSender(messageSender);

		if (!em.contains(game.getImplementation())) {
			Game test = new Game(em.find(game.getImplementation().getClass(), game.getId()));
			if (test.getImplementation() == null) 
				em.persist(game.getImplementation());
			else
				game = new Game (test.getImplementation());
		}
		
		if (!em.contains(registrant.getImplementation())) {
			Registrant test = new Registrant (em.find (
					registrant.getImplementation().getClass(), registrant.getId()));
			if (test.getImplementation() == null)
				em.persist(registrant.getImplementation());
			else
				registrant = new Registrant (test.getImplementation());
		}				

		registrant.setLastRegistration(System.currentTimeMillis());
		GamePlayer player = game.registerPlayer (registrant);		
		em.persist(player.getImplementation());			
		messageSender.sendPlayerChange(game.getImplementation());		
		return game;
	}
	

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.interfaces.RegistrarInterface#registerAgent(mx.ecosur.multigame.model.Game, mx.ecosur.multigame.model.Agent)
	 */
	public Game registerAgent(Game game, Agent agent) throws
		InvalidRegistrationException 
	{
        /* Set messaging */
        game.setMessageSender(messageSender);

		if (!em.contains(game.getImplementation())) {
			Game test = new Game(em.find(game.getImplementation().getClass(), game.getId()));
			if (test.getImplementation() == null) 
				em.persist(game.getImplementation());
			else
				game = new Game (test.getImplementation());
		}
		
		if (!em.contains(agent.getImplementation())) {
			Agent test = new Agent (em.find (
					agent.getImplementation().getClass(), agent.getId()));
			if (test.getImplementation() == null)
				em.persist(agent.getImplementation());
			else
				agent = new Agent (test.getImplementation());
		}		
		
		agent = game.registerAgent (agent);
		
		/* Merge changes*/
		em.merge(agent.getImplementation());
		em.merge(game.getImplementation());	
		
		messageSender.sendPlayerChange(game.getImplementation());		
		return game;		
	}		

	public Game unregister(Game game, GamePlayer player) throws InvalidRegistrationException {

		/* Remove the user from the Game */
		if (!em.contains(game.getImplementation()))
			game = new Game (em.find(game.getImplementation().getClass(), game.getId()));

		/* refresh the game object */
		em.refresh (game.getImplementation());

        /* Set messaging */
        game.setMessageSender(messageSender);

		game.removePlayer(player);
		game.setState(GameState.ENDED);
        messageSender.sendPlayerChange(game.getImplementation());	
        return game;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.RegistrarInterface#getUnfinishedGames(mx.ecosur.multigame.model.Registrant)
	 */
	public List<Game> getUnfinishedGames(Registrant player) {
        List<Game> ret = new ArrayList<Game>();
        Query query = em.createNamedQuery("getCurrentGames");
		query.setParameter("registrant", player.getImplementation());
		query.setParameter("state",GameState.ENDED);
		List<GameImpl> games = query.getResultList();
        for (GameImpl impl : games) {
            ret.add(new Game(impl));
        }

        return ret;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.RegistrarInterface#getPendingGames(mx.ecosur.multigame.model.Registrant)
	 */
	public List<Game> getPendingGames(Registrant player) {
        List<Game> ret = new ArrayList<Game>();        
        Query query = em.createNamedQuery("getAvailableGames");
		query.setParameter("registrant", player.getImplementation());
		query.setParameter("state", GameState.WAITING);
        /* HACK:  hand narrow the lists of pending games against unfinished */
        List<Game> joinedGames = getUnfinishedGames (player);        
		List<GameImpl> games = query.getResultList();
        for (GameImpl impl : games) {
            Game game = new Game (impl);
            if (joinedGames.contains(game))
                continue;
            ret.add(game);
        }

        return ret;
	}
}
