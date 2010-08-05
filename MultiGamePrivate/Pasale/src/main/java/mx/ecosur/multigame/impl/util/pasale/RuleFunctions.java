package mx.ecosur.multigame.impl.util.pasale;

import mx.ecosur.multigame.impl.entity.pasale.*;
import mx.ecosur.multigame.impl.enums.pasale.TokenType;
import mx.ecosur.multigame.impl.model.GridPlayer;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Nov 19, 2009
 * Time: 6:55:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuleFunctions {

    public static boolean isUnsupported (int supportSize, TablonFicha center, SortedSet<TablonFicha> set, TokenType type) {
        HashSet<TablonFicha> test = new HashSet<TablonFicha>();
        for (TablonFicha ficha : set) {
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

    public static TablonPlayer incrementTurn (TablonGame game, TablonMove move) {
        TablonPlayer player = (TablonPlayer) move.getPlayer();
        player.setTurn(false);

        List<GridPlayer> players = game.getPlayers();
        int playerNumber = players.indexOf(player);
        TablonPlayer nextPlayer = null;
        if (playerNumber == players.size() - 1) {
            nextPlayer = (TablonPlayer) players.get(0);
        } else {
            nextPlayer = (TablonPlayer) players.get(playerNumber + 1);
        }
        nextPlayer.setTurn (true);
        return nextPlayer;
    }

    public static boolean isBorder (TablonFicha ficha, int [] edges) {
        boolean ret = false;
        for (int i = 0; i < edges.length; i++) {
            if (ficha.getRow() == edges [ i ] || ficha.getColumn() == edges [ i ]) {
                ret = true;
                break;
            }
        }

        return ret;        
    }

    public static ArrayList<MutationEvent> findImplications (TablonGrid grid, Set implications) {
        ArrayList<MutationEvent> ret = new ArrayList<MutationEvent>();
        for (Object obj : implications) {
            TablonFicha ficha = (TablonFicha) obj;
            MutationEvent event = new MutationEvent (ficha);
            SortedSet<TablonFicha> square = grid.getSquare(ficha);
            event.setSquare (square);
            SortedSet<TablonFicha> octo = grid.getOctogon(ficha);
            event.setOctogon (octo);
            SortedSet<TablonFicha> cross = grid.getCross(ficha);
            event.setCross (cross);
            ret.add(event);
        }
        return ret;
    }

    public static int[] dimension (TablonGrid grid) {
        double size = Math.sqrt(grid.getCells().size());
        return new int [] { ((int) size / 2), ((int) size / 2) };
    }

    /* Water Path functions */
    public static boolean hasPathToWater (TablonFicha ficha, TablonGrid grid) {
        Stack<TablonFicha> path = getPathToWater (new Stack<TablonFicha>(), ficha, grid);
        return (path.size() > 0);
    }

    public static Set<Stack<TablonFicha>> getAllPathsToWater(TablonFicha ficha, TablonGrid grid) {
        HashSet<Stack<TablonFicha>> ret = new HashSet<Stack<TablonFicha>>();
        /* Get the original cross */
        for (TablonFicha test : grid.getCross(ficha)) {
             Stack<TablonFicha> path = getPathToWater (new Stack<TablonFicha>(), ficha, grid);
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
    private static Stack<TablonFicha> getPathToWater (Stack<TablonFicha> visited, TablonFicha ficha, TablonGrid grid)
    {
        visited.push(ficha);
        if (isConnectedToWater (ficha, grid)) {
           return visited;
        } else {
            Set<TablonFicha> cross = grid.getCross(ficha);
            for (TablonFicha crossFicha  : cross) {
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


    public static boolean isConnectedToWater (TablonFicha ficha, TablonGrid grid) {
        boolean ret = false;
        Set<TablonFicha> square = grid.getSquare(ficha);
        Set<TablonFicha> cross = grid.getCross(ficha);
        if (isDirectlyConnectedToWater (ficha, square)) {
            ret = true;
        } else {
            for (TablonFicha searchFicha : cross) {
                if (isDirectlyConnectedToWater (searchFicha, grid.getSquare(searchFicha))) {
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }

    public static boolean isDirectlyConnectedToWater (TablonFicha ficha, Set<TablonFicha> square) {
        boolean ret = false;
        for (TablonFicha searchFicha : square) {
            if (searchFicha.getType().equals(TokenType.WATER_PARTICLE)) {
                ret = true;
                break;
            }
        }

        return ret;
    }
}
