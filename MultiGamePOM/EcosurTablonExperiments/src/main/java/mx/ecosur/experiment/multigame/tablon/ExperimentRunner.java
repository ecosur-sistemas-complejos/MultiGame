package mx.ecosur.experiment.multigame.tablon;

import mx.ecosur.multigame.impl.entity.tablon.*;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import mx.ecosur.multigame.impl.model.GridCell;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.enums.tablon.TokenType;
import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.enums.GameState;
import mx.ecosur.multigame.enums.MoveStatus;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.io.ResourceFactory;
import org.drools.builder.*;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.io.File;
import java.io.FileWriter;

import com.mockrunner.jms.JMSTestCaseAdapter;
import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.mock.jms.MockTopic;

import javax.jms.Message;
import javax.jms.ObjectMessage;

/**
 * The Experiment runner runs a tablon experiment a certain number
 * of times (default 30) creating 
 */
public class ExperimentRunner extends JMSTestCaseAdapter {

    private static String dataFolder = "target/data";

    private static final int executions = 30;

    private static KnowledgeBase tablon;

    private static Random random;

    private TablonGame game;

    private EJBTestModule ejbModule;

	private MockTopic topic;

    private int retractions;


    /* Setup gente kbase */
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
        super.setUp();
        /* Set up mock JMS destination for message sender */
		ejbModule = createEJBTestModule();
		ejbModule.bindToContext("jms/TopicConnectionFactory",
				getJMSMockObjectFactory().getMockTopicConnectionFactory());
		//TODO: Externalize and change jndi name of topic
		topic = getDestinationManager().createTopic("MultiGame");
		ejbModule.bindToContext("MultiGame", topic);

        game = new TablonGame(tablon);
        GridRegistrant a, b, c, d;
		a = new GridRegistrant ("alice");
		b = new GridRegistrant ("bob");
		c = new GridRegistrant ("charlie");
		d = new GridRegistrant ("denise");

		game.registerPlayer(a);
		game.registerPlayer(b);
		game.registerPlayer(c);
		game.registerPlayer(d);

        /* Ensure that we recieved appropriate messages */
        List<Message> messageList = topic.getCurrentMessageList();
        if (messageList.size() == 0) {
            System.out.println ("No messages received from initialize!");
            System.exit (1);
        }

        retractions = 0;
    }

    public void runExperiment () {
        try {
            TablonFicha candidate = null;
            boolean remainingMoves = false;

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

            /* Pick a random Forest token */
            while (candidate == null) {
                int rand = random.nextInt(game.getGrid().getCells().size());
                GridCell cell = (GridCell) game.getGrid().getCells().toArray() [ rand ];
                TablonFicha ficha = (TablonFicha) cell;
                if (!ficha.getType().equals(TokenType.FOREST))
                    continue;
                else
                    candidate = ficha.clone();
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
            candidate.setType(TokenType.POTRERO);
            candidate.setColor(player.getColor());
            TablonMove move = new TablonMove (player, candidate);
            move = (TablonMove) game.move(move);

            ArrayList<Message> filter = new ArrayList<Message>();
            List<Message> messageList = topic.getCurrentMessageList();
            for (Message  message : messageList) {
                ObjectMessage msg = (ObjectMessage) message;
                if (message.getStringProperty("GAME_EVENT").equals("CONDITION_TRIGGERED")) {
                        this.retractions++;
                }
            }
            topic.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) throws InvalidRegistrationException, Exception {
        File file = new File (dataFolder + File.separator + "tablon-experiment.csv");
        file.mkdirs();
        if (file.exists())
            file.delete();
        file.createNewFile();
        random = new Random(System.currentTimeMillis());

        FileWriter writer = new FileWriter(file);
        writer.append ("Experiment No.");
        writer.append (",");
        writer.append ("Total Starting Tokens");
        writer.append (",");
        writer.append ("Elapsed Time");
        writer.append (",");
        writer.append ("Retraction Events");
        writer.append (",");
        writer.append ("Remaining Tokens");
        writer.append (",");
        writer.append ("Deduced Retractions");
        writer.append ("\n");
        writer.flush();

        long start = System.currentTimeMillis();

        for (int i = 1; i <= executions; i++) {
            long localStart = System.currentTimeMillis();
            System.out.println ("Run time(ms) = " + (System.currentTimeMillis () - start));
            System.out.println("Running iteration " + i);
            ExperimentRunner runner = new ExperimentRunner();
            runner.initialize();
            int startingTokens = runner.game.getGrid().getCells().size();
            while (runner.game.getState().equals(GameState.PLAY)) {
                runner.runExperiment();
                long elapsed = System.currentTimeMillis() - localStart;
                /* Serialize the data */
                writer.append ("" + i);
                writer.append (",");
                writer.append ("" + startingTokens);
                writer.append (",");
                writer.append ("" + elapsed);
                writer.append (",");
                writer.append ("" + runner.retractions);
                writer.append (",");
                writer.append ("" + runner.game.getGrid().getCells().size());
                writer.append (",");
                writer.append ("" + (startingTokens - runner.game.getGrid().getCells().size()));
                writer.append ("\n");
                writer.flush(); 
            }
            TablonGrid grid = (TablonGrid) runner.game.getGrid();
            System.out.println ("Water tokens: " + grid.getWaterTokens().size());
            System.out.println ("End configuration:\n" + grid.toString() + "\n");
        }
        /* close the data file */
        writer.close();
    }
}
