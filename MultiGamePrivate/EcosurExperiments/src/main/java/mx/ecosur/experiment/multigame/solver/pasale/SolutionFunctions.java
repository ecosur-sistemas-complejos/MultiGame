package mx.ecosur.experiment.multigame.solver.pasale;

import static mx.ecosur.multigame.impl.util.pasale.RuleFunctions.*;
import static mx.ecosur.experiment.multigame.solver.pasale.PasaleSolution.Quadrant;

import mx.ecosur.multigame.impl.entity.pasale.PasaleFicha;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGrid;

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

    public static boolean percentageDirectlyConnected (PasaleGrid grid, Collection fichas, double percentage) {
        HashSet<PasaleFicha> connected = new HashSet<PasaleFicha>();
        for (Object obj : fichas) {
            PasaleFicha ficha = (PasaleFicha) obj;
            if (isConnectedToWater (ficha, grid)) {
                connected.add(ficha);
            }
        }

        return (connected.size() == (int) percentage * fichas.size());
        
    }

    public static boolean percentagePerQuadrant (Dimension dimensions, Collection fichas, double percentage)
    {
        HashSet<PasaleFicha> tl = new HashSet<PasaleFicha>(), tr = new HashSet<PasaleFicha>(),
                bl = new HashSet<PasaleFicha>(), br = new HashSet<PasaleFicha>();
        for (Object obj : fichas) {
            PasaleFicha ficha = (PasaleFicha) obj;
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

    public static boolean percentageDirectlyConnectedPerQuadrant (PasaleGrid grid, Dimension dimensions,
          Collection<PasaleFicha> fichas, float percentage)
    {
        HashSet<PasaleFicha> tl = new HashSet<PasaleFicha>(), tr = new HashSet<PasaleFicha>(),
                bl = new HashSet<PasaleFicha>(), br = new HashSet<PasaleFicha>();
        for (PasaleFicha ficha : fichas) {
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
