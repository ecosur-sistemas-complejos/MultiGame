/**
 * 
 */
package mx.ecosur.multigame.checkers;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.Cell;

/**
 * @author awater
 * 
 */
public class Checker extends Cell {
	
	private static final long serialVersionUID = 2158082513951828819L;

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
