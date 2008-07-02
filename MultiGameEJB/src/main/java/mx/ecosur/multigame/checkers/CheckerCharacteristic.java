package mx.ecosur.multigame.checkers;

import java.io.Serializable;

import mx.ecosur.multigame.Characteristic;

public class CheckerCharacteristic implements Characteristic {
	
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
