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

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import mx.ecosur.multigame.MessageSender;
import mx.ecosur.multigame.PersistentRepository;
import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
import mx.ecosur.multigame.ejb.interfaces.RegistrarLocal;
import mx.ecosur.multigame.ejb.interfaces.RepositoryImpl;
import mx.ecosur.multigame.enums.GameState;

import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GamePlayer;
import mx.ecosur.multigame.model.Model;
import mx.ecosur.multigame.model.Registrant;
import mx.ecosur.multigame.model.implementation.GameImpl;
import mx.ecosur.multigame.model.implementation.RegistrantImpl;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class Registrar implements RegistrarRemote, RegistrarLocal {

	private MessageSender messageSender;
	
	@Resource
	private String repositoryImpl;
	
	PersistentRepository pr;

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
		RepositoryImpl impl = (RepositoryImpl) 
				Class.forName(repositoryImpl).newInstance();
		pr = new PersistentRepository (impl);
	}
	
	
	/**
	 * Registers a robot with she specified Game object.
	 * 
	 * TODO:  Make this generic.
	 * @throws InvalidRegistrationException 
	 */
	
	public GamePlayer registerAgent (Game game, Registrant registrant) 
		throws InvalidRegistrationException 
	{
		if (!pr.contains(game.getImplementation()))
			game = new Game((GameImpl) pr.find(GameImpl.class, game.getId()));
		
		if (!pr.contains(registrant)) {
			Registrant test = new Registrant ((RegistrantImpl) 
					pr.find (RegistrantImpl.class, registrant.getId()));
			if (test == null)
				pr.persist(registrant.getImplementation());
		}				

		registrant.setLastRegistration(System.currentTimeMillis());
		GamePlayer player = game.registerPlayer (registrant);		
		pr.persist(player.getImplementation());		
		
		messageSender.sendPlayerChange(game);
		pr.flush();
		return player;	
	}

	public void unregisterPlayer(GamePlayer player) 
		throws 
	InvalidRegistrationException {

		/* Remove the user from the Game */
		Game game = player.getGame();
		if (!pr.contains(game))
			game = new Game ((GameImpl) pr.find(GameImpl.class, game.getId()));

		/* refresh the game object */
		pr.refresh (game.getImplementation());
		game.removePlayer(player);
		game.setState(GameState.END);
		pr.flush();
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.RegistrarInterface#getUnfinishedGames(mx.ecosur.multigame.model.Registrant)
	 */
	public List<Game> getUnfinishedGames(Registrant player) 
	{
		List<Model> results = pr.executeNamedQuery ("getGamesByPlayer", player);
		List<Game> ret = new ArrayList<Game>();
		for (Model model : results) {
			ret.add( (Game) model);
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.RegistrarInterface#getPendingGames(mx.ecosur.multigame.model.Registrant)
	 */
	public List<Game> getPendingGames(Registrant player) {
		List <Model> results = pr.executeNamedQuery ("getGamesByNotPlayer", player, 
				State.WAITING);
		List<Game> ret = new ArrayList<Game>();
		for (Model model : results) {
			ret.add( (Game) model);
		}
		
		return ret;		
	}
}
