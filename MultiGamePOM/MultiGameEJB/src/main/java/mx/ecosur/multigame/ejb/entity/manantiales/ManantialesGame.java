package mx.ecosur.multigame.ejb.entity.manantiales;

import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.exception.CheckConstraint;
import mx.ecosur.multigame.manantiales.Mode;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import java.util.ArrayList;

@Entity
public class ManantialesGame extends Game {
	
	private static final long serialVersionUID = -8395074059039838349L;
	
	private Mode mode; 
	
	private ArrayList<CheckConstraint> checkConstraints;
    
    @Enumerated (EnumType.STRING)
    public Mode getMode() {
        return mode;
    }
		
    public void setMode (Mode mode) {
        this.mode = mode;
    }
    
    public ArrayList<CheckConstraint> getCheckConstraints () {
    	return checkConstraints;
    }
    
    public void setCheckConstraints (ArrayList<CheckConstraint> checkConstraints) {
    	this.checkConstraints = checkConstraints;
    }
    
    public void addCheckConstraint (CheckConstraint violation) {
    	if (checkConstraints == null) 
    		checkConstraints = new ArrayList<CheckConstraint>();
    	checkConstraints.add(violation);
    }
}
