package mx.ecosur.experiment.multigame.solver.tablon;

import static mx.ecosur.multigame.impl.util.pasale.RuleFunctions.*;
import static mx.ecosur.experiment.multigame.solver.tablon.TablonSolution.Quadrant;

import mx.ecosur.multigame.impl.entity.pasale.TablonFicha;
import mx.ecosur.multigame.impl.entity.pasale.TablonGrid;

import java.util.HashSet;
import java.util.Collection;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Dec 11, 2009
 * Time: 3:28:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class SolutionFunctions {

    public static boolean percentageDirectlyConnected (TablonGrid grid, Collection fichas, double percentage) {
        HashSet<TablonFicha> connected = new HashSet<TablonFicha>();
        for (Object obj : fichas) {
            TablonFicha ficha = (TablonFicha) obj;
            if (isConnectedToWater (ficha, grid)) {
                connected.add(ficha);
            }
        }

        return (connected.size() == (int) percentage * fichas.size());
        
    }

    public static boolean percentagePerQuadrant (Dimension dimensions, Collection fichas, double percentage)
    {
        HashSet<TablonFicha> tl = new HashSet<TablonFicha>(), tr = new HashSet<TablonFicha>(), 
                bl = new HashSet<TablonFicha>(), br = new HashSet<TablonFicha>();
        for (Object obj : fichas) {
            TablonFicha ficha = (TablonFicha) obj;
            Point point = new Point (ficha.getRow(),ficha.getColumn());
            if (Quadrant.TOPLEFT.contains(dimensions, point)) {
                tl.add(ficha);
            } else if (Quadrant.TOPRIGHT.contains (dimensions, point)) {
                tr.add(ficha);
            } else if (Quadrant.BOTTOMLEFT.contains (dimensions, point)) {
                bl.add(ficha);
            } else if (Quadrant.BOTTOMRIGHT.contains (dimensions, point)) {
                br.add(ficha);
            }
        }

        /* TODO: determine dynamic quadrant size and evaluate pieces per set */

        return false;
    }

    public static boolean percentageDirectlyConnectedPerQuadrant (TablonGrid grid, Dimension dimensions,
          Collection<TablonFicha> fichas, float percentage)
    {
        HashSet<TablonFicha> tl = new HashSet<TablonFicha>(), tr = new HashSet<TablonFicha>(),
                bl = new HashSet<TablonFicha>(), br = new HashSet<TablonFicha>();
        for (TablonFicha ficha : fichas) {
            Point point = new Point (ficha.getRow(),ficha.getColumn());
            if (Quadrant.TOPLEFT.contains(dimensions, point)) {
                tl.add(ficha);
            } else if (Quadrant.TOPRIGHT.contains (dimensions, point)) {
                tr.add(ficha);
            } else if (Quadrant.BOTTOMLEFT.contains (dimensions, point)) {
                bl.add(ficha);
            } else if (Quadrant.BOTTOMRIGHT.contains (dimensions, point)) {
                br.add(ficha);
            }
        }

        return (percentageDirectlyConnected (grid, tl, percentage) && percentageDirectlyConnected (grid, tr, percentage)
                && percentageDirectlyConnected (grid, bl, percentage) && percentageDirectlyConnected (grid, br, percentage));

    }
}
