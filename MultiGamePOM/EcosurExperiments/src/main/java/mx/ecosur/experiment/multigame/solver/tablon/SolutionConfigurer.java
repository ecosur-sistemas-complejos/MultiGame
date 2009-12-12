package mx.ecosur.experiment.multigame.solver.tablon;

import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.impl.DummyMessageSender;
import mx.ecosur.multigame.impl.entity.tablon.TablonGame;
import mx.ecosur.multigame.impl.entity.tablon.TablonPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.io.ResourceFactory;
import org.drools.builder.*;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Dec 11, 2009
 * Time: 10:08:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SolutionConfigurer {

    TablonSolution solution;

    public SolutionConfigurer (TablonSolution solution) {
        this.solution = solution;
    }


    public TablonSolution configure () throws InvalidRegistrationException {
        KnowledgeBase tablon = KnowledgeBaseFactory.newKnowledgeBase();
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

        TablonGame game = new TablonGame(26, 26, tablon);
        game.setMessageSender(new DummyMessageSender());

		GridRegistrant a, b, c, d;
		a = new GridRegistrant ("alice");
		b = new GridRegistrant ("bob");
		c = new GridRegistrant ("charlie");
		d = new GridRegistrant ("denise");

		game.registerPlayer(a);
		game.registerPlayer(b);
		game.registerPlayer(c);
		game.registerPlayer(d);

        solution.setGame(game);

        return solution;
    }
}
