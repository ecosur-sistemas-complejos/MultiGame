package mx.ecosur.experiment.multigame.solver.tablon;

import org.drools.solver.core.move.factory.AbstractMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

import java.util.*;

import mx.ecosur.multigame.impl.entity.tablon.TablonFicha;
import mx.ecosur.multigame.impl.entity.tablon.TablonGrid;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;

/**
 * The CarefulMoveSuggestor is an implmentation of AbstractMoveFactory
 * that seeks to suggest "CarefulMoves" to the solver.
 */
public class TablonMoveSuggestor extends AbstractMoveFactory {


    public List<Move> createMoveList(Solution solution) {
        List<Move> moves = new ArrayList<Move>();        
        TablonSolution tsol = (TablonSolution) solution;
        for (GridCell cell : tsol.getGrid().getCells()) {
            TablonFicha ficha = (TablonFicha) cell;
            if (ficha.getType().equals(TokenType.POTRERO)) {
                Set<Stack<TablonFicha>> pathways = getAllPathsToWater (ficha, tsol.getGrid());
                for (Stack<TablonFicha> moveStack : pathways) {
                    while (!moveStack.isEmpty()) {
                        moves.add(new PotreroMove(moveStack.pop()));
                    }
                }
            }
        }

        return moves;
    }

    private Set<Stack<TablonFicha>> getAllPathsToWater(TablonFicha center, TablonGrid grid) {
        HashSet<Stack<TablonFicha>> ret = new HashSet<Stack<TablonFicha>>();
        /* Get the original cross */
        for (TablonFicha ficha : grid.getCross(center)) {
             Stack<TablonFicha> path = getPathToWater (new Stack<TablonFicha>(), center, grid);
            if (path.size() > 0)
                ret.add(path);
        }

        return ret;
    }

    private Stack<TablonFicha> getPathToWater (Stack<TablonFicha> visited,
             TablonFicha ficha, TablonGrid grid)
    {
        visited.push(ficha);
        if (isConnectedToWater (ficha, grid)) {
           return visited;
        } else {
            Set<TablonFicha> cross = grid.getCross(ficha);
            for (TablonFicha crossFicha  : cross) {
                if (crossFicha.getType().equals(TokenType.POTRERO) && !visited.contains(crossFicha)) {
                    return getPathToWater (visited, crossFicha, grid);
                }
            }
        }

        visited.clear();
        return visited;
    }


    private boolean isConnectedToWater (TablonFicha ficha, TablonGrid grid) {
        boolean ret = false;
        for (TablonFicha searchFicha : grid.getSquare(ficha)) {
            if (searchFicha.getType().equals(TokenType.WATER_PARTICLE)) {
                ret = true;
                break;
            }
        }

        return ret;
    }
}
