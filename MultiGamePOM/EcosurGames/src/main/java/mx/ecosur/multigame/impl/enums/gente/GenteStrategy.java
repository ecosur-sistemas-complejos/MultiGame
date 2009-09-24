/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.0. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * Enum for articulating playing strategies and rulesbases for those
 * strategies.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.impl.enums.gente;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.KnowledgeBase;
import org.drools.io.ResourceFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;

public enum GenteStrategy {
	
	RANDOM, BLOCKER, SIMPLE;
	
	private KnowledgeAgent kagent;

    private static Logger logger = Logger.getLogger(GenteStrategy.class
            .getCanonicalName());

    public KnowledgeBase getRuleBase() {
        /* Check that rule set has not already been created */
        if (kagent == null) {
            logger.fine("Initializing knowledge agent for type " + this);
            /* Setup the knowledge agent */
            kagent = KnowledgeAgentFactory.newKnowledgeAgent(
                    this + "Agent");
        }

        switch (this) {
            case BLOCKER:
                kagent.applyChangeSet(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream(
                        "/mx/ecosur/multigame/impl/blocker-agent.xml")));
                break;
            case RANDOM:
                kagent.applyChangeSet(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream(
                        "/mx/ecosur/multigame/impl/blocker-agent.xml")));
                break;
            case SIMPLE:
                kagent.applyChangeSet(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream(
                        "/mx/ecosur/multigame/impl/blocker-agent.xml")));
                break;
            default:
                break;
        }

        return kagent.getKnowledgeBase();

    }
}
