package mx.ecosur.multigame.ejb.entity.manantiales;

import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.manantiales.CheckCondition;
import mx.ecosur.multigame.manantiales.ConditionType;
import mx.ecosur.multigame.manantiales.Mode;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import java.util.HashSet;
import java.util.Set;

@Entity
public class ManantialesGame extends Game {
	
	private static final long serialVersionUID = -8395074059039838349L;
	
	private Mode mode; 
	
	private HashSet<CheckCondition> checkConstraints;
    
    @Enumerated (EnumType.STRING)
    public Mode getMode() {
        return mode;
    }
		
    public void setMode (Mode mode) {
    	this.checkConstraints = null;
        this.mode = mode;
    }
    
    public boolean hasCondition (ConditionType type) {
    	boolean ret = false;
    	if (checkConstraints != null) {
	    	for (CheckCondition condition : checkConstraints) {
	    		if (condition.getReason().equals(type)) {
	    			ret = true;
	    		}
	    	}
    	}
	    	
    	return ret;
    }
    
    public HashSet<CheckCondition> getCheckConstraints () {
    	return checkConstraints;
    }
    
    public void setCheckConstraints (HashSet<CheckCondition> checkConstraints) {
    	this.checkConstraints = checkConstraints;
    }
    
    public void addCheckConstraint (CheckCondition violation) {
    	if (checkConstraints == null) 
    		checkConstraints = new HashSet<CheckCondition>();
    	if (!hasCondition (violation.getReason()))
    		checkConstraints.add(violation);
    }

	/* (non-Javadoc)
	 * @see mx.ecosur.multigame.ejb.entity.Game#getFacts()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set getFacts() {
		Set facts = super.getFacts();
		if (checkConstraints != null)
			facts.addAll(checkConstraints);
		return facts;
	}
}
