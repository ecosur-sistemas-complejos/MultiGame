/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.edu.multigame.drools;

import java.util.logging.Logger;

import org.drools.event.ObjectInsertedEvent;
import org.drools.event.ObjectRetractedEvent;
import org.drools.event.ObjectUpdatedEvent;
import org.drools.event.WorkingMemoryEventListener;

public class DebugEventListener implements WorkingMemoryEventListener {	
	private Logger logger;
	
	public DebugEventListener () {
		logger = Logger.getLogger(DebugEventListener.class.getCanonicalName());
	}

	public void objectInserted(ObjectInsertedEvent event) {
		logger.info(event.getObject().toString());
	}

	public void objectRetracted(ObjectRetractedEvent event) {
		logger.info(event.getOldObject().toString());
	}

	public void objectUpdated(ObjectUpdatedEvent event) {
		logger.info (event.getObject().toString());
	}
}
