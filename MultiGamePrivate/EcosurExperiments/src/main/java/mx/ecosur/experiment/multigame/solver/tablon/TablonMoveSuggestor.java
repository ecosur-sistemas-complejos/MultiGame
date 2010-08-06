package mx.ecosur.experiment.multigame.solver.tablon;

import mx.ecosur.multigame.impl.entity.pasale.PasaleFicha;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGame;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGrid;
import mx.ecosur.multigame.impl.enums.pasale.TokenType;
import org.drools.solver.core.move.factory.AbstractMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

import java.util.*;

import mx.ecosur.multigame.impl.model.GridCell;

import static mx.ecosur.multigame.impl.util.pasale.RuleFunctions.*;

/**
 * The CarefulMoveSuggestor is an implmentation of AbstractMoveFactory
 * that seeks to suggest "CarefulMoves" to the solver.
 */
public class TablonMoveSuggestor extends AbstractMoveFactory {

    public List<Move> createMoveList(Solution solution) {
        List<Move> moves = new ArrayList<Move>();        
        TablonSolution tsol = (TablonSolution) solution;
        PasaleGame game = tsol.getGame();
        for (GridCell cell : tsol.getGame().getGrid().getCells()) {
            PasaleFicha ficha = (PasaleFicha) cell;
            if (ficha.getType().equals(TokenType.FOREST) || ficha.getType().equals(TokenType.POTRERO)) {
                Set<Stack<PasaleFicha>> pathways = getAllPathsToWater (ficha, (PasaleGrid) game.getGrid());
                for (Stack<PasaleFicha> moveStack : pathways) {
                    while (!moveStack.isEmpty()) {
                        PasaleGame clone = null;
                        try {
                            clone = (PasaleGame) game.clone();
                            moves.add(new PotreroMove(clone, moveStack.pop()));
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }

                if (ficha.getType().equals(TokenType.POTRERO)) {
                    PasaleGame clone = null;
                    try {
                        clone = (PasaleGame) game.clone();
                        moves.add(new SilvoPastoralMove(clone, ficha));
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }

        return moves;
    }
}