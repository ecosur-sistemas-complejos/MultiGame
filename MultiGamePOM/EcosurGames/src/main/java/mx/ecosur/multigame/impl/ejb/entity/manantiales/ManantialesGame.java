package mx.ecosur.multigame.impl.ejb.entity.manantiales;

import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.impl.manantiales.ConditionType;
import mx.ecosur.multigame.impl.manantiales.Mode;
import mx.ecosur.multigame.model.Game;
import mx.ecosur.multigame.model.GameGrid;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NamedQueries( {
	@NamedQuery(name = "getManantialesGame", query = "select g from ManantialesGame g where g.type=:type "
		+ "and g.state =:state"),
	@NamedQuery(name = "getManantialesGameById", query = "select g from ManantialesGame g where g.id=:id "),
	@NamedQuery(name = "getManantialesGameByTypeAndPlayer", query = "select mp.game from ManantialesPlayer as mp "
		+ "where mp.player=:player and mp.game.type=:type and mp.game.state <>:state")
})
@Entity
public class ManantialesGame extends Game {
	
	private static final long serialVersionUID = -8395074059039838349L;
	
	private Mode mode; 
	
	private Set<CheckCondition> checkConditions;
    
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
	    		if (condition.getReason().equals(type)) {
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
    	if (!hasCondition (violation.getReason()))
    		checkConditions.add(violation);
    }

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Game#getFacts()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set getFacts() {
		Set facts = super.getFacts();
		if (checkConditions != null)
			facts.addAll(checkConditions);
		return facts;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Game#initialize(mx.ecosur.multigame.GameType)
	 */
	@Override
	public Game initialize(GameType type) {
		this.setGrid(new GameGrid());
		this.setState(GameState.WAITING);
		this.setCreated(new Date());
		this.setType(type);		
		this.setColumns (9);
		this.setRows(9);
		return this;
	}

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.model.Game#getMaxPlayers()
	 */
	@Override
	public int getMaxPlayers() {
		return 4;
	}
}
