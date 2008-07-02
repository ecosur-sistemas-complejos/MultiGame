/**
 * 
 */
package mx.ecosur.multigame.checkers;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.Color;

/**
 * @author awater
 * 
 */
public class Checker extends Cell {
	
	public Checker(){
		super();
	}
	
	/**
	 * @param x
	 * @param y
	 * @param color
	 */
	public Checker(int x, int y, Color color) {
		super(x, y, color);
		setCharacteristic(new CheckerCharacteristic());
	}

	public Checker clone() throws CloneNotSupportedException {
		Checker clone = new Checker(getRow(), getColumn(), getColor());
		if (getCharacteristic() != null)
			clone.setCharacteristic(getCharacteristic().clone());
		return clone;
	}
}
