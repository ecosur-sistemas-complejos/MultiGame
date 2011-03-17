package mx.ecosur.multigame.impl.util.pasale;

import mx.ecosur.multigame.impl.entity.pasale.*;
import mx.ecosur.multigame.impl.enums.pasale.TokenType;
import mx.ecosur.multigame.grid.model.GridPlayer;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Nov 19, 2009
 * Time: 6:55:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuleFunctions {

    public static boolean isUnsupported (int supportSize, PasaleFicha center, SortedSet<PasaleFicha> set, TokenType type) {
        HashSet<PasaleFicha> test = new HashSet<PasaleFicha>();
        for (PasaleFicha ficha : set) {
            if (ficha == null)
                continue;
            if (ficha.equals(center))
                continue;
            if (ficha.getType().equals(type))
                test.add(ficha);
            if (test.size() >= supportSize)
                break;
        }

        return test.size() < supportSize;
    }

    public static PasalePlayer incrementTurn (PasaleGame game, PasaleMove move) {
        GridPlayer player = move.getPlayer();
        player.setTurn(false);

        /* Find next player */
        Set<GridPlayer> players = game.getPlayers();
        GridPlayer [] gps = players.toArray(new GridPlayer[players.size()]);
        int playerNumber = -1;

        for (int i = 0; i < gps.length; i++) {
            if (gps [ i ].equals(player)) {
                playerNumber = i;
                break;
            }
        }

        if (playerNumber == -1)
            throw new RuntimeException ("Unable to find player: " + player + " in set " + gps);

        GridPlayer nextPlayer = null;
        if (playerNumber == gps.length - 1) {
            nextPlayer = gps [ 0 ];
        } else {
            nextPlayer = gps [playerNumber + 1];
        }

        nextPlayer.setTurn (true);
        return (PasalePlayer) nextPlayer;
    }

    public static boolean isBorder (PasaleFicha ficha, int [] edges) {
        boolean ret = false;
        for (int i = 0; i < edges.length; i++) {
            if (ficha.getRow() == edges [ i ] || ficha.getColumn() == edges [ i ]) {
                ret = true;
                break;
            }
        }

        return ret;        
    }

    public static ArrayList<MutationEvent> findImplications (PasaleGrid grid, Set implications) {
        ArrayList<MutationEvent> ret = new ArrayList<MutationEvent>();
        for (Object obj : implications) {
            PasaleFicha ficha = (PasaleFicha) obj;
            MutationEvent event = new MutationEvent (ficha);
            SortedSet<PasaleFicha> square = grid.getSquare(ficha);
            event.setSquare (square);
            SortedSet<PasaleFicha> octo = grid.getOctogon(ficha);
            event.setOctogon (octo);
            SortedSet<PasaleFicha> cross = grid.getCross(ficha);
            event.setCross (cross);
            ret.add(event);
        }
        return ret;
    }

    public static int[] dimension (PasaleGrid grid) {
        double size = Math.sqrt(grid.getCells().size());
        return new int [] { ((int) size / 2), ((int) size / 2) };
    }

    /* Water Path functions */
    public static boolean hasPathToWater (PasaleFicha ficha, PasaleGrid grid) {
        Stack<PasaleFicha> path = getPathToWater (new Stack<PasaleFicha>(), ficha, grid);
        return (path.size() > 0);
    }

    public static Set<Stack<PasaleFicha>> getAllPathsToWater(PasaleFicha ficha, PasaleGrid grid) {
        HashSet<Stack<PasaleFicha>> ret = new HashSet<Stack<PasaleFicha>>();
        /* Get the original cross */
        for (PasaleFicha test : grid.getCross(ficha)) {
             Stack<PasaleFicha> path = getPathToWater (new Stack<PasaleFicha>(), ficha, grid);
            if (path.size() > 0)
                ret.add(path);
        }

        return ret;
    }

    /* A path is found by searching the Potrero's CROSS for all
        connected potreros and/or Forested sites.  Each poterero
        is then queried for its SQUARE to see if it contains a WATER_PARTICLE.
        If there is no water_particle, a search is made to see if there is
        another
     */
    private static Stack<PasaleFicha> getPathToWater (Stack<PasaleFicha> visited, PasaleFicha ficha, PasaleGrid grid)
    {
        visited.push(ficha);
        if (isConnectedToWater (ficha, grid)) {
           return visited;
        } else {
            Set<PasaleFicha> cross = grid.getCross(ficha);
            for (PasaleFicha crossFicha  : cross) {
                if ( (crossFicha.getType().equals(TokenType.POTRERO)) && 
                        !visited.contains(crossFicha))
                {
                    return getPathToWater (visited, crossFicha, grid);
                }
            }
        }

        visited.clear();
        return visited;
    }    


    public static boolean isConnectedToWater (PasaleFicha ficha, PasaleGrid grid) {
        boolean ret = false;
        Set<PasaleFicha> square = grid.getSquare(ficha);
        Set<PasaleFicha> cross = grid.getCross(ficha);
        if (isDirectlyConnectedToWater (ficha, square)) {
            ret = true;
        } else {
            for (PasaleFicha searchFicha : cross) {
                if (isDirectlyConnectedToWater (searchFicha, grid.getSquare(searchFicha))) {
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }

    public static boolean isDirectlyConnectedToWater (PasaleFicha ficha, Set<PasaleFicha> square) {
        boolean ret = false;
        for (PasaleFicha searchFicha : square) {
            if (searchFicha.getType().equals(TokenType.WATER_PARTICLE)) {
                ret = true;
                break;
            }
        }

        return ret;
    }
}
