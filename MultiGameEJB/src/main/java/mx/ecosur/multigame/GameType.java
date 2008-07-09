package mx.ecosur.multigame;

import mx.ecosur.multigame.ejb.entity.Player;

public enum GameType {
	
	CHECKERS, PENTE;

	public String getNamedQuery () {
		if (this.equals(GameType.PENTE))
			return "getPenteGame";
		else 
			return "getGameByType";
	}
	
	public String getNamedQuery (int someValue) {
		if (this.equals(GameType.PENTE))
			return "getPenteGameById";
		else 
			return "getGameById";
	}
}
