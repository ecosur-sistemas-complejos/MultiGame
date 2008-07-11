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
	
	public String getNamedQueryById() {
		if (this.equals(GameType.PENTE))
			return "getPenteGameById";
		else 
			return "getGameById";
	}
	
	public String getNamedPlayerQuery () {
		if (this.equals(GameType.PENTE))
			return "getPenteGamePlayer";
		else
			return "getGamePlayer";
	}
}
