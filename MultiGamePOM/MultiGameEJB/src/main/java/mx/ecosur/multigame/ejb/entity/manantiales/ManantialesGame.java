package mx.ecosur.multigame.ejb.entity.manantiales;

import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.manantiales.CheckConstraint;
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
	
	private HashSet<CheckConstraint> checkConstraints;
    
    @Enumerated (EnumType.STRING)
    public Mode getMode() {
        return mode;
    }
		
    public void setMode (Mode mode) {
        this.mode = mode;
    }
    
    public HashSet<CheckConstraint> getCheckConstraints () {
    	return checkConstraints;
    }
    
    public void setCheckConstraints (HashSet<CheckConstraint> checkConstraints) {
    	this.checkConstraints = checkConstraints;
    }
    
    public void addCheckConstraint (CheckConstraint violation) {
    	if (checkConstraints == null) 
    		checkConstraints = new HashSet<CheckConstraint>();
    	if (checkConstraints.contains(violation))
    		checkConstraints.remove(violation);
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
