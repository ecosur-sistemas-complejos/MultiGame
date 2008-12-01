/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.ai.experiment;

import org.apache.myfaces.trinidad.model.BoundedRangeModel;

public class GenteRobotBoundedRangeModel extends BoundedRangeModel {
	
	public GenteRobotBoundedRangeModel () {
		super();
		this.maximum = 0;
		this.current = 0;
	}
	
	/**
	 * @param maximum
	 */
	public GenteRobotBoundedRangeModel(long maximum) {
		super();
		this.maximum = maximum;
		this.current = 0;
	}

	private long current, maximum;

	/* (non-Javadoc)
	 * @see org.apache.myfaces.trinidad.model.BoundedRangeModel#getMaximum()
	 */
	@Override
	public long getMaximum() {
		return maximum;
	}
	
	public void setMaximum(long max) {
		maximum = max;
		current = 0;
	}

	/* (non-Javadoc)
	 * @see org.apache.myfaces.trinidad.model.BoundedRangeModel#getValue()
	 */
	@Override
	public long getValue() {
		return current;
	}
	
	public void updateCurrent () {
		current++;
	}

}
