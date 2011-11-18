package mx.ecosur.multigame.impl.util.manantiales;

import mx.ecosur.multigame.grid.*;
import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.entity.GridCell;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesPlayer;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.multigame.model.interfaces.GamePlayer;
import mx.ecosur.multigame.model.interfaces.Move;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static mx.ecosur.multigame.impl.util.manantiales.RuleFunctions.*;
/**
 */
public class MovesValidator {

    private ManantialesGame game;

    private List<Move> moves;

    private List<Move> validated;

    public MovesValidator(ManantialesGame game, List<Move> moves) {
        this.game = game;
        this.moves = moves;
        this.validated = Collections.EMPTY_LIST;
    }

    public List<Move> getValidated() {
        return validated;
    }

    /* Vists and validates moves that were given to this visitor on Construction */
    public void visit () {
        for (Move m : moves) {
            ManantialesMove move = (ManantialesMove) m;
            if (valid(move)) {
                if (validated == Collections.EMPTY_LIST)
                    validated = new ArrayList<Move>();
                if (!validated.contains(move))
                    validated.add(move);
            }
        }
    }

    private boolean valid (ManantialesMove move) {
        boolean ret = true;
        AdjGraph graph = game.getGraph();

        /* Check for adjacent intensives */
        if (move.getType().equals(TokenType.INTENSIVE_PASTURE)) {
            Point destination = new Point(move.getDestinationCell().getRow(), move.getDestinationCell().getColumn());
            int id = graph.findNode(destination);
            List<Point> connected = getConnections(destination);
            for (Point p : connected) {
                ManantialesFicha test = (ManantialesFicha) game.getGrid().getLocation(new GridCell(p.y,p.x, Color.UNKNOWN));
                if (test.getType().equals(TokenType.INTENSIVE_PASTURE)) {
                    ret = false;
                    break;
                }
            }
        }

        /* Check for too much deforestation */
        if (move.getType().equals(TokenType.MODERATE_PASTURE) || move.getType().equals(TokenType.INTENSIVE_PASTURE)) {
            int available = 48;
            for (GamePlayer p : game.getPlayers()) {
                ManantialesPlayer player = (ManantialesPlayer) p;
                available -= player.getIntensive() + player.getModerate();
            }

            ret = available > 18; // 48 - 30 == 18;
        }

        /* A few checks from RuleFunctions */
        ret = ret && isValidReplacement(game.getGrid(), move);
        ret = ret && isWithinTimeLimit(game, move);

        return ret;
    }

    public List<Point> getConnections(Point point) {
        List<Point> connections = Collections.EMPTY_LIST;
        AdjGraph graph = game.getGraph();
        int loc = graph.findNode(point);
        for (int i = 0; i < graph.size(); i++) {
            if (i != loc)
                if (graph.containsEdge(loc, i)) {
                    if (connections == Collections.EMPTY_LIST)
                        connections = new ArrayList<Point>();
                    connections.add(graph.findPoint(i));
                }
        }

        return Collections.EMPTY_LIST;
    }


    public List<Move> getValidMoves() {
        return validated;
    }
}
