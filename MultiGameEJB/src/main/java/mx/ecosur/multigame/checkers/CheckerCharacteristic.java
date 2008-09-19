package mx.ecosur.multigame.checkers;

import mx.ecosur.multigame.Characteristic;

public class CheckerCharacteristic implements Characteristic {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5815949939979111669L;
	private boolean kinged;

	public CheckerCharacteristic () {
		super();
		this.kinged = false;
	}
	
	public CheckerCharacteristic (boolean kinged) {
		super();
		this.kinged = true;
	}

	public boolean isKinged() {
		return kinged;
	}

	public void setKinged(boolean kinged) {
		this.kinged = kinged;
	}
	
	public CheckerCharacteristic clone() {
		CheckerCharacteristic ret = new CheckerCharacteristic ();
		ret.setKinged(this.isKinged());
		return ret;
	}
	
	public String toString(){
		return "kinged = " + kinged;
	}
}
