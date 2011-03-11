/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.experiment.multigame.solver.manantiales;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
import mx.ecosur.experiment.multigame.solver.manantiales.ManantialesSolution.Threshold;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class SolutionConfigurer {
	
	ManantialesSolution solution;
	
	public SolutionConfigurer (ManantialesSolution solution) {
		this.solution = solution;
	}
	
	public Document load (String resourcePath) throws JDOMException, IOException {
		 InputStreamReader reader = new InputStreamReader(
				 this.getClass().getResourceAsStream(resourcePath));
		 SAXBuilder builder = new SAXBuilder();
		 return builder.build(reader);
	}
	
	@SuppressWarnings("unchecked")
	public ManantialesSolution configure (Document document) throws UnconfigurableException {
		solution.initialize();
		/* Configure the game */
		Element solutionElement = document.getRootElement();
		/* Get the threshold */
		solution.setThreshold (ManantialesSolution.Threshold.valueOf (
				solutionElement.getAttributeValue("type")));
		List <Element> settors = solutionElement.getChildren();
		for (Element tok : settors) {
			if (tok.getName().equals("token")) {
				int col = Integer.parseInt(tok.getChild("column").getText());
				int row = Integer.parseInt(tok.getChild("row").getText());
				Color color = Color.valueOf(tok.getChild("color").getText());
				TokenType type = TokenType.valueOf(tok.getChild("type").getText());
				SolverFicha token = new SolverFicha(col, row, color, type);
				solution.replaceToken(token);
			} else if (tok.getName().equals("distribution")) {
				Color color = Color.valueOf(tok.getChild("color").getText());
				int forest = Integer.parseInt(tok.getChild("forest").getText());
				int moderate = Integer.parseInt(tok.getChild("moderate").getText());
				int intensive = Integer.parseInt(tok.getChild("intensive").getText());
				int silvo = 0;
				if (solution.getThreshold().equals(ManantialesSolution.Threshold.INNOVATIVE))
					silvo = Integer.parseInt(tok.getChild("silvopastoral").getText());
				Distribution dist = new Distribution(color, forest, moderate,
						intensive, silvo);
				solution.addDistribution(dist);
				populateDistribution(solution , dist);
			}
		}
		
		return solution;
	}
	
	public ManantialesSolution configure (Matrix matrix) throws UnconfigurableException {
		solution.initialize();
		if (matrix.getCount() > 48)
			throw new RuntimeException ("Only 48 locations available to be " +
					"colonized!");
		for (Distribution distribution : matrix.getDistributions()) {
			solution.addDistribution(distribution);
			populateDistribution(solution, distribution);
		}
		return solution;
	}

	/**
	 * @param solution
	 * @param dist
	 * @throws UnconfigurableException 
	 */
	private void populateDistribution(ManantialesSolution solution,
			Distribution dist) throws UnconfigurableException 
	{
		if (dist.getCount() > 16) 
			throw new RuntimeException ("Distributions are from 8 - 16!");
		
		int forest = dist.getForest(), moderate = dist.getModerate(),
			intensive = dist.getIntensive(), silvo = dist.getSilvopastoral();

		/* Populate Core territory first */
		for (ManantialesFicha tok : solution.getTokens()) {
			if (tok.getColor().equals(dist.getColor())) {
				if (forest > 0) {
					tok.setType(TokenType.MANAGED_FOREST);
					forest--;
				} else if (moderate > 0) {
					tok.setType (TokenType.MODERATE_PASTURE);
					moderate--;
				} else if (intensive > 0) {
					tok.setType(TokenType.INTENSIVE_PASTURE);
					intensive--;
				} else if (solution.getThreshold().equals(ManantialesSolution.Threshold.INNOVATIVE) &&
						silvo > 0) 
				{
					tok.setType (TokenType.SILVOPASTORAL);
					silvo--;
				}
			}
		}
		
		if (forest > 0 || moderate > 0 || intensive > 0 || silvo > 0) {
			for (ManantialesFicha tok : solution.getUndevelopedBorders(dist.getColor())) {
				tok.setColor(dist.getColor());
				if (forest > 0) {
					tok.setType(TokenType.MANAGED_FOREST);
					forest--;
				} else if (moderate > 0) {
					tok.setType (TokenType.MODERATE_PASTURE);
					moderate--;
				} else if (intensive > 0) {
					tok.setType(TokenType.INTENSIVE_PASTURE);
					intensive--;
				} else if (solution.getThreshold().equals(Threshold.INNOVATIVE) &&
						silvo > 0) 
				{
					tok.setType (TokenType.SILVOPASTORAL);
					silvo--;
				}
			}
		}
		
		if (forest > 0 || moderate > 0 || intensive > 0 || silvo > 0)
			throw new UnconfigurableException (
					"Unable to configure distribution! Not enough space for" +
					"distribution: [" + dist + "]");
	}
}
