/**
 * 
 */
package mx.ecosur.multigame.ui;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

import mx.ecosur.multigame.Color;

/**
 * The TokenIcon is used to display player's icons in the
 * MultiGame.  This class paints a Java2D ellipse in the 
 * Graphics context, coloring this ellipse to match the 
 * Player's chosen color.
 * 
 * @author awater
 *
 */
public class Token extends JPanel {
	
	private Color color;

	public Token (Color color) {
		this.color = color;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paint (Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setBackground(this.getBackground());
		
		switch (color) {
			case BLACK:
				g2.setColor(java.awt.Color.DARK_GRAY);
				break;
			case BLUE:
				g2.setColor (java.awt.Color.BLUE);
				break;
			case GREEN:
				g2.setColor (java.awt.Color.GREEN);
				break;
			case RED:
				g2.setColor (java.awt.Color.RED);
				break;
			default:
				break;
		}
		
		Rectangle bounds = this.getBounds();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	             RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.draw(new Ellipse2D.Double(5, 5, bounds.getWidth() - 10, bounds.getHeight() - 10));
		GradientPaint gradiant = new GradientPaint(30, 30, g2.getColor().brighter(), 
				(int) bounds.getWidth() - 60, (int) bounds.getHeight() - 60, 
				g2.getColor().darker());
		g2.setPaint(gradiant);
		g2.fillOval (5, 5, (int) bounds.getWidth() - 10, (int) bounds.getHeight() - 10);
		
		g2.setColor(g2.getColor().darker().darker());
	    g2.setStroke(new BasicStroke(3));
		g2.draw (new Ellipse2D.Double(10, 10, bounds.getWidth() - 20, bounds.getHeight() - 20));
		g2.draw (new Ellipse2D.Double(15, 15, bounds.getWidth() - 30, bounds.getHeight() - 30));
	}
}
