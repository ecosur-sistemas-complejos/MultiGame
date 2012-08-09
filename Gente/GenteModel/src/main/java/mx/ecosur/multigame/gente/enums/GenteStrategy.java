/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
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

package mx.ecosur.multigame.gente.enums;

import java.util.logging.Logger;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.io.ResourceFactory;

public enum GenteStrategy {
        
    RANDOM, SIMPLE;
        
    private transient KnowledgeBase kbase;

    private static Logger logger = Logger.getLogger(GenteStrategy.class
            .getCanonicalName());

    public KnowledgeBase getRuleBase() {
        /* Check that rule set has not already been created */
        KnowledgeBuilder kbuilder  = null;
        if (kbase == null) {
            kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            switch (this) {
                case RANDOM:
                    kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                        "/mx/ecosur/multigame/gente/random-agent.xml")), ResourceType.CHANGE_SET);
                    break;
                case SIMPLE:
                    kbuilder.add(ResourceFactory.newInputStreamResource(getClass().getResourceAsStream (
                        "/mx/ecosur/multigame/gente/simple-agent.xml")), ResourceType.CHANGE_SET);
                    break;
                default:
                    break;
            }

            if (kbuilder != null) {
                kbase = KnowledgeBaseFactory.newKnowledgeBase();
                KnowledgeBuilderErrors errors = kbuilder.getErrors();
                StringBuffer message = new StringBuffer ();
                for (KnowledgeBuilderError error : errors) {
                    message.append (error.getMessage());
                }

                if (message.length() > 0) {
                    kbase = null;
                    logger.warning("Errors creating knowledge builder " + message.toString());
                  throw new RuntimeException (message.toString());
                } else
                    kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
            }
        }

        return kbase;
    }

    @Override
    public String toString () {
        return this.name() + " kabase loaded? " + (kbase != null);
    }
}
