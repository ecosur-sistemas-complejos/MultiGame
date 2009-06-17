package mx.ecosur.multigame.impl.ejb.entity.manantiales;

import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.impl.manantiales.ConditionType;
import mx.ecosur.multigame.impl.manantiales.Mode;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

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
	 * @see mx.ecosur.multigame.ejb.entity.Game#getFacts()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set getFacts() {
		Set facts = super.getFacts();
		if (checkConditions != null)
			facts.addAll(checkConditions);
		return facts;
	}
}
