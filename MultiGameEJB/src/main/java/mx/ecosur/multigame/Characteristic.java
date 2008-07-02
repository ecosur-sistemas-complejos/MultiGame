/**
 * 
 */
package mx.ecosur.multigame;

import java.io.Serializable;

/**
 * Characteristics are specific to the game being modeled.  Each cell has
 * a "characteristic" field which allows implementors to express different
 * modes for specific games.  For example, Checkers has a characteristic that
 * defines a piece as being regular or a "King".
 * 
 * @author awater
 *
 */
public interface Characteristic extends Serializable, Cloneable {
	
	public Characteristic clone() throws CloneNotSupportedException;

}
