package mx.ecosur.experiment.multigame.solver.pasale;

import mx.ecosur.multigame.exception.InvalidRegistrationException;
import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.grid.DummyMessageSender;
import mx.ecosur.multigame.impl.entity.pasale.PasaleGame;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.io.ResourceFactory;

/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: Dec 11, 2009
 * Time: 10:08:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SolutionConfigurer {

    PasaleSolution solution;

    public SolutionConfigurer (PasaleSolution solution) {
        this.solution = solution;
    }


    public PasaleSolution configure () throws InvalidRegistrationException {
        KnowledgeBase tablon = KnowledgeBaseFactory.newKnowledgeBase();
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newInputStreamResource(PasaleGame.class.getResourceAsStream (
            "/mx/ecosur/multigame/impl/tablon.drl")), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newInputStreamResource(PasaleGame.class.getResourceAsStream (
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

        PasaleGame game = new PasaleGame(18,18);
        game.setKbase(tablon);
        game.setMessageSender(new DummyMessageSender());

		GridRegistrant a, b, c, d;
		a = new GridRegistrant ("alice");
		b = new GridRegistrant ("bob");
		c = new GridRegistrant ("charlie");
		d = new GridRegistrant("denise");

		game.registerPlayer(a);
		game.registerPlayer(b);
		game.registerPlayer(c);
		game.registerPlayer(d);

        /* Reset solution to use constructed game */
        solution = new PasaleSolution(game);

        return solution;
    }
}
