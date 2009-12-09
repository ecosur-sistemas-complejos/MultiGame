package mx.ecosur.experiment.multigame.solver.tablon;

import org.drools.solver.core.move.factory.AbstractMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

import java.util.*;

import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.entity.tablon.TablonGrid;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;

import static mx.ecosur.multigame.impl.util.tablon.RuleFunctions.*;

/**
 * The CarefulMoveSuggestor is an implmentation of AbstractMoveFactory
 * that seeks to suggest "CarefulMoves" to the solver.
 */
public class TablonMoveSuggestor extends AbstractMoveFactory {


    public List<Move> createMoveList(Solution solution) {
        List<Move> moves = new ArrayList<Move>();        
        TablonSolution tsol = (TablonSolution) solution;
        TablonGrid grid = tsol.getGrid();
        for (GridCell cell : grid.getCells()) {
            TablonFicha ficha = (TablonFicha) cell;
            if (ficha.getType().equals(TokenType.POTRERO)) {
                Set<Stack<TablonFicha>> pathways = getAllPathsToWater (ficha, tsol.getGrid());
                for (Stack<TablonFicha> moveStack : pathways) {
                    while (!moveStack.isEmpty()) {
                        moves.add(new PotreroMove(moveStack.pop(), grid));
                    }
                }

                moves.add(new SilvoPastoralMove(ficha, grid));
            }           
        }

        return moves;
    }
}