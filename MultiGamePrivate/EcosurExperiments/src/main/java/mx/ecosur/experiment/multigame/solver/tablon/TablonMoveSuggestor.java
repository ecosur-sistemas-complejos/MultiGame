package mx.ecosur.experiment.multigame.solver.tablon;

import mx.ecosur.multigame.impl.enums.tablon.TokenType;
import org.drools.solver.core.move.factory.AbstractMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

import java.util.*;

import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.entity.tablon.TablonGrid;
import mx.ecosur.multigame.impl.entity.tablon.TablonGame;
import mx.ecosur.multigame.impl.model.GridCell;

import static mx.ecosur.multigame.impl.util.tablon.RuleFunctions.*;

/**
 * The CarefulMoveSuggestor is an implmentation of AbstractMoveFactory
 * that seeks to suggest "CarefulMoves" to the solver.
 */
public class TablonMoveSuggestor extends AbstractMoveFactory {

    public List<Move> createMoveList(Solution solution) {
        List<Move> moves = new ArrayList<Move>();        
        TablonSolution tsol = (TablonSolution) solution;
        TablonGame game = tsol.getGame();
        for (GridCell cell : tsol.getGame().getGrid().getCells()) {
            TablonFicha ficha = (TablonFicha) cell;
            if (ficha.getType().equals(TokenType.FOREST) || ficha.getType().equals(TokenType.POTRERO)) {
                Set<Stack<TablonFicha>> pathways = getAllPathsToWater (ficha, (TablonGrid) game.getGrid());
                for (Stack<TablonFicha> moveStack : pathways) {
                    while (!moveStack.isEmpty()) {
                        TablonGame clone = null;
                        try {
                            clone = (TablonGame) game.clone();
                            moves.add(new PotreroMove(clone, moveStack.pop()));
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }

                if (ficha.getType().equals(TokenType.POTRERO)) {
                    TablonGame clone = null;
                    try {
                        clone = (TablonGame) game.clone();
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