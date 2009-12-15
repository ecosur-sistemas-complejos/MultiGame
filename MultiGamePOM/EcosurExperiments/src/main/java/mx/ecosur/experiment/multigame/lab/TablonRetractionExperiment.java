package mx.ecosur.experiment.multigame.lab;

import mx.ecosur.multigame.impl.entity.tablon.*;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;
import mx.ecosur.multigame.impl.DummyMessageSender;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.enums.GameState;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.io.ResourceFactory;
import org.drools.builder.*;

import java.util.*;
import java.io.File;
import java.io.FileWriter;

/**
 * The Experiment runner runs a tablon experiment a certain number
 * of times (default 30) creating 
 */
public class TablonRetractionExperiment {

    private static final int dimension = 42;

    private static String dataFolder = "target/data";

    private static final int executions = 30;

    private static final char separator = '\t';

    private static KnowledgeBase tablon;

    private static Random random;

    private TablonGame game;

    private int retractions;

    private int moves;


    /* Setup gen
    te kbase */
    static {
        tablon = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(TablonGame.class.getResourceAsStream (
            "/mx/ecosur/multigame/impl/tablon.drl")), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newInputStreamResource(TablonGame.class.getResourceAsStream (
            "/mx/ecosur/multigame/impl/ruleflow/tablon-flow.rf")), ResourceType.DRF);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (errors.size() == 0)
            tablon.addKnowledgePackages(kbuilder.getKnowledgePackages());
        else {
            for (KnowledgeBuilderError error : errors) {
                System.out.println(error);
            }

            throw new RuntimeException ("Unable to load rule base!");
        }
    }

    public void initialize () throws InvalidRegistrationException, Exception {
        game = new TablonGame(dimension, dimension, tablon);
        game.setMessageSender (new DummyMessageSender());
        GridRegistrant a, b, c, d;
		a = new GridRegistrant ("alice");
		b = new GridRegistrant ("bob");
		c = new GridRegistrant ("charlie");
		d = new GridRegistrant ("denise");

		game.registerPlayer(a);
		game.registerPlayer(b);
		game.registerPlayer(c);
		game.registerPlayer(d);

        retractions = 0;
        moves = 0;
    }

    public void runExperiment () {
        try {
            TablonFicha candidate = null;
            boolean remainingMoves = false;
            TablonGrid tgrid = (TablonGrid) game.getGrid();

            /* Search for 5 times the number of cells in the grid */
            if (moves > (5 * game.getGrid().getCells().size())) {
                game.setState(GameState.ENDED);
                return;
            }

            for (GridCell cell : game.getGrid().getCells()) {
                TablonFicha ficha = (TablonFicha) cell;
                if (ficha.getType().equals(TokenType.FOREST)) {
                    remainingMoves = true;
                    break;
                }
            }

            if (!remainingMoves) {
                game.setState(GameState.ENDED);
                return;
            }

            /* Pick a potrero and extend it, otherwise,
               pick a random Forest token */
            while (candidate == null) {
                Set<TablonFicha> potreros = new HashSet<TablonFicha> ();
                for (GridCell cell : game.getGrid().getCells()) {
                    TablonFicha ficha = (TablonFicha) cell;
                    if (ficha.getType().equals(TokenType.POTRERO) || ficha.getType().equals(TokenType.SILVOPASTORAL))
                        potreros.add(ficha);
                }

                if (potreros.size() > 0) {
                    Object[] set = potreros.toArray();
                    TablonFicha potrero = (TablonFicha) set [ random.nextInt(set.length) ];
                    if (true || random.nextInt(6) < 4) {
                        for (TablonFicha ficha : tgrid.getCross(potrero)) {
                            if (ficha.getType().equals(TokenType.FOREST) && extendsPotrero (ficha, potrero)) {
                                candidate = ficha.clone();
                                candidate.setType (TokenType.POTRERO);
                                break;
                            }
                        }
                    } else if (!potrero.getType().equals(TokenType.SILVOPASTORAL)) {
                        candidate = potrero.clone();
                        candidate.setType(TokenType.SILVOPASTORAL);
                    }
                }

                if (candidate == null) {
                    int rand = random.nextInt(game.getGrid().getCells().size());
                    GridCell cell = (GridCell) game.getGrid().getCells().toArray() [ rand ];
                    TablonFicha ficha = (TablonFicha) cell;
                    if (ficha.getType().equals(TokenType.FOREST)) {
                        candidate = ficha.clone();
                        candidate.setType (TokenType.POTRERO);
                        break;
                    } else if (ficha.getType().equals(TokenType.POTRERO)) {
                        candidate = ficha.clone();
                        candidate.setType (TokenType.SILVOPASTORAL);
                        break;
                    }
                }
            }

            /* If the candiate is still null; there are no moves left to make
               so end the game.
             */

            TablonPlayer player = null;
            for (GridPlayer p : game.getPlayers()) {
                if (p.isTurn()) {
                    player = (TablonPlayer) p;
                    break;
                }
            }

            candidate.setColor(player.getColor());
            TablonMove move = new TablonMove (player, candidate);
            move = (TablonMove) game.move(move);
            moves++;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean extendsPotrero (TablonFicha ficha, TablonFicha potrero) {
        boolean ret = false;

        int middle = dimension/2;
            /* 4 quadrants */
        if (potrero.getColumn() < middle && potrero.getRow() < middle) {
            ret = (ficha.getColumn () < potrero.getColumn() && ficha.getRow () < potrero.getRow());
        } else if (potrero.getColumn() > middle && potrero.getRow() < middle) {
            ret = (ficha.getColumn() > potrero.getColumn() && ficha.getRow() < potrero.getRow());
        } else if (potrero.getColumn() < middle && potrero.getRow () > middle) {
            ret = (ficha.getColumn() < potrero.getColumn() && ficha.getRow() > potrero.getRow());
        } else if (potrero.getColumn() > middle && potrero.getRow () > middle) {
            ret = (ficha.getColumn() > potrero.getColumn() && ficha.getRow () > potrero.getRow());
        }

        return ret;

    }

    public static void main (String... args) throws InvalidRegistrationException, Exception {
        File file = new File (dataFolder + File.separator + "lab-experiment.csv");
        file.mkdirs();
        if (file.exists())
            file.delete();
        file.createNewFile();
        random = new Random(System.currentTimeMillis());

        FileWriter writer = new FileWriter(file);
        writer.append ("Experiment No.");
        writer.append (separator);
        writer.append ("Step No.");
        writer.append (separator);
        writer.append ("Elapsed Time (ms)");
        writer.append (separator);
        writer.append ("Starting Tokens");
        writer.append (separator);
        writer.append ("Total Retractions");
        writer.append (separator);
        for (int i = 1; i <= executions; i++) {
            writer.append ("Retractions Experiment-" + i);
            if (i <= executions)
                writer.append (separator);
            if (i == executions)
                writer.append ("\n");
        }
        writer.flush();

        long start = System.currentTimeMillis();

        for (int i = 1; i <= executions; i++) {
            String tabs = "";
            long localStart = System.currentTimeMillis();
            System.out.println ("Run time(ms) = " + (System.currentTimeMillis () - start));
            System.out.println("Running iteration " + i);
            TablonRetractionExperiment tablonRetraction = new TablonRetractionExperiment();
            tablonRetraction.initialize();
            int startingTokens = tablonRetraction.game.getGrid().getCells().size();
            int counter = 0;
            while (tablonRetraction.game.getState().equals(GameState.PLAY)) {
                counter++;
                tablonRetraction.runExperiment();
                long elapsed = System.currentTimeMillis() - localStart;
                /* One execution is streamed to the console change by change */
                if (executions == 1) {
                    System.out.println ("Step No. " + counter);
                    System.out.println (tablonRetraction.game);
                }
                /* Serialize the data */
                writer.append ("" + i);
                writer.append (separator);
                writer.append ("" + counter);
                writer.append (separator);
                writer.append ("" + elapsed);
                writer.append (separator);
                writer.append ("" + startingTokens);
                writer.append (separator);
                writer.append ("" + (startingTokens - tablonRetraction.game.getGrid().getCells().size()));
                writer.append (separator);
                for (int j = 1; j <= executions; j++) {
                    if (j == i)
                        writer.append ("" + (startingTokens - tablonRetraction.game.getGrid().getCells().size()));
                    else if (j < executions)
                        writer.append (separator);
                    else if (j == executions)
                        writer.append ("\n");
                }
                writer.flush();
            }
        }
        /* close the data file */
        writer.close();
    }    
}
