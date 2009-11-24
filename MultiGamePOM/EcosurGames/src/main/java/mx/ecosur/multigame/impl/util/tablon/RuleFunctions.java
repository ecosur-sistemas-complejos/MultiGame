package mx.ecosur.multigame.impl.util.tablon;

import mx.ecosur.multigame.impl.entity.tablon.*;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;
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

    public static boolean isUnary (TablonFicha center, SortedSet<TablonFicha> set, TokenType type) {
        HashSet<TablonFicha> test = new HashSet<TablonFicha>();
        for (TablonFicha ficha : set) {
            if (ficha == null)
                continue;
            if (ficha.equals(center))
                continue;
            if (ficha.getType().equals(type))
                test.add(ficha);
            if (test.size() > 1)
            	break;
        }

        return test.size() == 1;
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
            /* Borders are ignored */
            if (ficha.getRow () == 1 || ficha.getColumn() == 1)
                continue;
            MutationEvent event = new MutationEvent (ficha);
            SortedSet square = grid.getSquare(ficha);
            event.setSquare (square);
            SortedSet octo = grid.getOctogon(ficha);
            event.setOctogon (octo);
            ret.add(event);
        }
        return ret;
    }
}
