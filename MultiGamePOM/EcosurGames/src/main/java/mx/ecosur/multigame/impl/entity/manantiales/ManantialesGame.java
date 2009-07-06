package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.GridGame;
import mx.ecosur.multigame.impl.model.GameGrid;

import mx.ecosur.multigame.impl.enums.manantiales.ConditionType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;

import mx.ecosur.multigame.model.implementation.GamePlayerImpl;
import mx.ecosur.multigame.model.implementation.Implementation;
import mx.ecosur.multigame.model.implementation.MoveImpl;
import mx.ecosur.multigame.model.implementation.RegistrantImpl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NamedQueries( {
	@NamedQuery(name = "getManantialesGame", query = "select g from ManantialesGame g where g.type=:type "
		+ "and g.state =:state"),
	@NamedQuery(name = "getManantialesGameById", query = "select g from ManantialesGame g where g.id=:id "),
	@NamedQuery(name = "getManantialesGameByTypeAndPlayer", query = "select mp.game from ManantialesPlayer as mp "
		+ "where mp.player=:player and mp.game.type=:type and mp.game.state <>:state")
})
@Entity
public class ManantialesGame extends GridGame {
	
	private static final long serialVersionUID = -8395074059039838349L;
	
	private Mode mode; 
	
	private Set<CheckCondition> checkConditions;

	private RuleBase ruleBase;
    
    @Enumerated (EnumType.STRING)
    public Mode getMode() {
        return mode;
    }
		
    public void setMode (Mode mode) {
    	this.checkConditions = null;
        this.mode = mode;
    }
    
    public boolean hasCondition (ConditionType type) {
    	boolean ret = false;
    	if (checkConditions != null) {
	    	for (CheckCondition condition : checkConditions) {
	    		if (condition.getType().equals(type)) {
	    			ret = true;
	    		}
	    	}
    	}
	    	
    	return ret;
    }
    
    @OneToMany (fetch=FetchType.EAGER)
    public Set<CheckCondition> getCheckConditions () {
    	if (checkConditions == null)
    		checkConditions = new HashSet<CheckCondition>();
    	return checkConditions;
    }
    
    public void setCheckConditions (Set<CheckCondition> checkConstraints) {
    	this.checkConditions = checkConstraints;
    }
    
    public void addCheckCondition (CheckCondition violation) {
    	if (checkConditions == null) 
    		checkConditions = new HashSet<CheckCondition>();
    	if (!hasCondition (ConditionType.valueOf(violation.getReason())))
    		checkConditions.add(violation);
    }

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Game#getFacts()
	 */
	@Override
	public Set<Implementation> getFacts() {
		Set<Implementation> facts = super.getFacts();
		if (checkConditions != null)
			facts.addAll(checkConditions);
		return facts;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Game#initialize(mx.ecosur.multigame.GameType)
	 */
	public void initialize() {
		this.setGrid(new GameGrid());
	    this.setState(GameState.BEGIN);
	    this.setCreated(new Date());
		this.setColumns(9);
		this.setRows(9);
		
		if (ruleBase == null) {
			PackageBuilder builder = new PackageBuilder();
		    InputStreamReader reader = new InputStreamReader(
		    		getClass().getResourceAsStream(
		    				"/mx/ecosur/multigame/impl/manantiales.drl"));
		    try {
				builder.addPackageFromDrl(reader);
			} catch (DroolsParserException e) {
				e.printStackTrace();
				throw new RuntimeException (e);
			} catch (IOException e) {			
				e.printStackTrace();
				throw new RuntimeException (e);
			}
		    ruleBase = RuleBaseFactory.newRuleBase();
		    ruleBase.addPackage(builder.getPackage());
		}
		
		StatefulSession session = ruleBase.newStatefulSession();
		session.insert(this);
		for (Object fact : getFacts()) {
			session.insert(fact);
		}
		session.setFocus("initialize");
		session.fireAllRules();
		session.dispose();						
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Game#getMaxPlayers()
	 */
	@Override
	public int getMaxPlayers() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#move(mx.ecosur.multigame.model.implementation.MoveImpl)
	 */
	@Override
	public void move(MoveImpl move) throws InvalidMoveException {
		try {
			if (ruleBase == null) {
				PackageBuilder builder = new PackageBuilder();
			    InputStreamReader reader = new InputStreamReader(
			    		getClass().getResourceAsStream(
			    				"/mx/ecosur/multigame/impl/manantiales.drl"));
			    builder.addPackageFromDrl(reader);
			    ruleBase = RuleBaseFactory.newRuleBase();
			    ruleBase.addPackage(builder.getPackage());
			}
			
			/* Make the move */
			StatefulSession session = ruleBase.newStatefulSession();
			session.insert(this);
			session.insert(move);
			for (Implementation fact : getFacts()) {
				session.insert(fact);
			}
			
			session.setFocus("verify");
		    session.fireAllRules();
		    session.setFocus("move");
		    session.fireAllRules();
		    session.setFocus("evaluate");
		    session.fireAllRules();
		    session.dispose();			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidMoveException (e.getMessage());
		}			
	}
	
	public GamePlayerImpl registerPlayer(RegistrantImpl registrant)  {			
		ManantialesPlayer player = new ManantialesPlayer ();
		player.setPlayer(registrant);
		player.setGame(this);
		
		int max = getMaxPlayers();
		if (players.size() == max)
			throw new RuntimeException ("Maximum Players reached!");
		
		List<Color> colors = getAvailableColors();
		player.setColor(colors.get(0));		
		players.add(player);
		
		/* If we've reached the max, then set the GameState to begin */
		if (players.size() == max)
			state = GameState.BEGIN;
		/* Be sure that the player has a good reference to this game */
		player.setGame(this);
		
		return player;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.impl.model.GridGame#getColors()
	 */
	@Override
	public List<Color> getColors() {
		List<Color> ret = new ArrayList<Color>();
		for (Color color : Color.values()) {
			if (color.equals(Color.UNKNOWN))
				continue;
			if (color.equals(Color.GREEN))
				continue;
			ret.add(color);
		}
		
		return ret;
	}	
}
