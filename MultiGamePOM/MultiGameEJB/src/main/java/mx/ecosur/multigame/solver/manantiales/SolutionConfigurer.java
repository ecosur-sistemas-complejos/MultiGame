/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.solver.manantiales;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import mx.ecosur.multigame.CellComparator;
import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.ejb.entity.manantiales.Token;
import mx.ecosur.multigame.manantiales.TokenType;

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
	
	public ManantialesSolution configure (Document document) {
		/* Configure the game */
		Element solutionElement = document.getRootElement();
		List <Element> settors = solutionElement.getChildren();
		for (Element tok : settors) {
			int col = Integer.parseInt(tok.getChild("column").getText());
			int row = Integer.parseInt(tok.getChild("row").getText());
			Color color = Color.valueOf(tok.getChild("color").getText());
			TokenType type = TokenType.valueOf(tok.getChild("type").getText());
			Token token = new Token (col, row, color, type);
			solution.replaceToken(token);
		}
		return solution;
	}
}