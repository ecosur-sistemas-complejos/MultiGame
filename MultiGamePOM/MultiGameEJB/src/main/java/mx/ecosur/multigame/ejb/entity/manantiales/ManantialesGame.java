package mx.ecosur.multigame.ejb.entity.manantiales;

import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.manantiales.Mode;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@SuppressWarnings("serial")
@Entity
public class ManantialesGame extends Game {
	
    private Mode mode; 
    
    @Enumerated (EnumType.STRING)
    public Mode getMode() {
        return mode;
    }
		
    public void setMode (Mode mode) {
        this.mode = mode;
    }
}
