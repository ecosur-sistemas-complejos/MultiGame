package mx.ecosur.experiment.multigame.solver.tablon;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.core.move.Move;
import org.drools.WorkingMemory;
import org.drools.FactHandle;
import mx.ecosur.multigame.impl.entity.pasale.TablonFicha;
import mx.ecosur.multigame.impl.entity.pasale.TablonGame;
import mx.ecosur.multigame.impl.entity.pasale.TablonMove;
import mx.ecosur.multigame.impl.enums.pasale.TokenType;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.enums.MoveStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Dec 9, 2009
 * Time: 9:19:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SilvoPastoralMove implements Move {

    TablonFicha ficha;

    TablonGame game;

    public SilvoPastoralMove (TablonGame game, TablonFicha ficha) {
        this.game = game;
        this.ficha = ficha;
    }


    /**
     * Called before a move is evaluated to decide wheter the move can be done and evaluated.
     * A Move isn't doable if:
     * <ul>
     * <li>Either doing it would change nothing in the solution.</li>
     * <li>Either it's simply not possible to do.</li>
     * </ul>
     * Although you could filter out non-doable moves in for example the {@link org.drools.solver.core.move.factory.MoveFactory},
     * this is not needed as the {@link org.drools.solver.core.Solver} will do it for you.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} not yet modified by the move.
     * @return true if the move achieves a change in the solution and the move is possible to do on the solution.
     */
    public boolean isMoveDoable(WorkingMemory workingMemory) {
        boolean ret = false;
        try {
            GridPlayer current = null;
            for (GridPlayer player : game.getPlayers()) {
                if (player.isTurn()) {
                    current = player;
                    break;
                }
            }

            /* must be a player with a turn */
            assert (current != null);
            int starting = game.getGrid().getCells().size();
            TablonMove move = new TablonMove (current, ficha);
            move = (TablonMove) game.move(move);
            int ending = game.getGrid().getCells().size();

            /* To be valid, the move must have been evaluated and no retractions occurred */
            ret = (move.getStatus().equals(MoveStatus.EVALUATED) && starting == ending);
        } catch (InvalidMoveException e) {
           //
        }

        return ret;
}

    /**
     * Called before the move is done, so the move can be evaluated and then be undone
     * without resulting into a permanent change in the solution.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} not yet modified by the move.
     * @return an undoMove which does the exact opposite of this move.
     */
    public Move createUndoMove(WorkingMemory workingMemory) {
        UndoMove ret = null;
        try {
            TablonFicha undo = ficha.clone();
            ret = new UndoMove ((TablonGame) game.clone(), undo, ficha);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ret;
    }

    /**
     * Does the Move and updates the {@link org.drools.solver.core.solution.Solution} and its {@link org.drools.WorkingMemory} accordingly.
     * When the solution is modified, the {@link org.drools.WorkingMemory}'s {@link org.drools.FactHandle}s should be correctly notified,
     * otherwise the score(s) calculated will be corrupted.
     *
     * @param workingMemory the {@link org.drools.WorkingMemory} that needs to get notified of the changes.
     */
    public void doMove(WorkingMemory workingMemory) {      
        FactHandle handle = workingMemory.getFactHandle((TablonFicha) game.getGrid().getLocation(ficha));
        workingMemory.modifyRetract(handle);
        ficha.setType(TokenType.SILVOPASTORAL);
        workingMemory.modifyInsert(handle,ficha);
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * <p/>
     * The general contract of <code>hashCode</code> is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     * an execution of a Java application, the <tt>hashCode</tt> method
     * must consistently return the same integer, provided no information
     * used in <tt>equals</tt> comparisons on the object is modified.
     * This integer need not remain consistent from one execution of an
     * application to another execution of the same application.
     * <li>If two objects are equal according to the <tt>equals(Object)</tt>
     * method, then calling the <code>hashCode</code> method on each of
     * the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     * according to the {@link Object#equals(Object)}
     * method, then calling the <tt>hashCode</tt> method on each of the
     * two objects must produce distinct integer results.  However, the
     * programmer should be aware that producing distinct integer results
     * for unequal objects may improve the performance of hashtables.
     * </ul>
     * <p/>
     * As much as is reasonably practical, the hashCode method defined by
     * class <tt>Object</tt> does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the
     * Java<font size="-2"><sup>TM</sup></font> programming language.)
     *
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see java.util.Hashtable
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(ficha);
        return builder.toHashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p/>
     * The <code>equals</code> method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     * <code>x</code>, <code>x.equals(x)</code> should return
     * <code>true</code>.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     * <code>x</code> and <code>y</code>, <code>x.equals(y)</code>
     * should return <code>true</code> if and only if
     * <code>y.equals(x)</code> returns <code>true</code>.
     * <li>It is <i>transitive</i>: for any non-null reference values
     * <code>x</code>, <code>y</code>, and <code>z</code>, if
     * <code>x.equals(y)</code> returns <code>true</code> and
     * <code>y.equals(z)</code> returns <code>true</code>, then
     * <code>x.equals(z)</code> should return <code>true</code>.
     * <li>It is <i>consistent</i>: for any non-null reference values
     * <code>x</code> and <code>y</code>, multiple invocations of
     * <tt>x.equals(y)</tt> consistently return <code>true</code>
     * or consistently return <code>false</code>, provided no
     * information used in <code>equals</code> comparisons on the
     * objects is modified.
     * <li>For any non-null reference value <code>x</code>,
     * <code>x.equals(null)</code> should return <code>false</code>.
     * </ul>
     * <p/>
     * The <tt>equals</tt> method for class <code>Object</code> implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values <code>x</code> and
     * <code>y</code>, this method returns <code>true</code> if and only
     * if <code>x</code> and <code>y</code> refer to the same object
     * (<code>x == y</code> has the value <code>true</code>).
     * <p/>
     * Note that it is generally necessary to override the <tt>hashCode</tt>
     * method whenever this method is overridden, so as to maintain the
     * general contract for the <tt>hashCode</tt> method, which states
     * that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof SilvoPastoralMove) {
            SilvoPastoralMove comp = (SilvoPastoralMove) obj;
            ret = (comp.ficha.equals(this.ficha));
        } else {
            ret = super.equals(obj);
        }

        return ret;
    }
}

