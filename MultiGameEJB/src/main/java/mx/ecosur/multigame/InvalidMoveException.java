/**
 * 
 */
package mx.ecosur.multigame;

/**
 * 
 * The InvalidMoveException describes the problem with a specific move
 * against a shared board.
 * 
 * @author awater
 *
 */
@SuppressWarnings("serial")
public class InvalidMoveException extends Exception {

	public InvalidMoveException(String message) {
		super (message);
	}

}
